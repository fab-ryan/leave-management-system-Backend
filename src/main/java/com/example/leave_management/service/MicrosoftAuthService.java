package com.example.leave_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.util.JwtUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MicrosoftAuthService {

    @Value("${microsoft.client-id}")
    private String clientId;

    @Value("${microsoft.client-secret}")
    private String clientSecret;

    @Value("${microsoft.redirect-uri}")
    private String redirectUri;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${allowed.email.domain}")
    private String allowedEmailDomain;

    @Autowired
    private EmployeeService employeeService;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromHttpUrl("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile email User.Read User.ReadBasic.All")
                .queryParam("response_mode", "query")
                .build()
                .toUriString();
    }

    public ResponseEntity<?> handleMicrosoftCallback(String code) {
        try {
            Map<String, Object> tokenResponse = getAccessToken(code);
            String accessToken = (String) tokenResponse.get("access_token");

            Map<String, Object> userInfo = getUserInfo(accessToken);
            String email = (String) userInfo.get("mail");
            String name = (String) userInfo.get("displayName");

            // Check email domain in production mode
            if ("prod".equals(activeProfile) && !email.endsWith("@" + allowedEmailDomain)) {
                String errorMessage = "Access forbidden. Only " + allowedEmailDomain + " email addresses are allowed.";
                String errorUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                        .path("/login")
                        .queryParam("error", errorMessage)
                        .build()
                        .encode()
                        .toUriString();

                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", errorUrl)
                        .build();
            }

            // Get profile picture
            String picture = getProfilePicture(accessToken);

            ResponseEntity<?> response = employeeService.handleOAuth2Login(email, name, picture);

            // Extract token from response
            String token = null;
            UserRole role = UserRole.EMPLOYEE;
            if (response.getBody() instanceof ApiResponse) {
                ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
                Map<String, Object> responseData = (Map<String, Object>) apiResponse.getData();
                token = (String) responseData.get("token");
                role = (UserRole) responseData.get("role");
            }

            // Create redirect URL with token
            String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                    .path("/login")
                    .queryParam("token", token)
                    .queryParam("role", role)
                    .build()
                    .encode()
                    .toUriString();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        } catch (Exception e) {
            // If there's an error, redirect to frontend error page
            String errorMessage = "Authentication failed";
            System.out.println("Error: " + e.getMessage());
            String errorUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl)
                    .path("/login")
                    .queryParam("error", errorMessage)
                    .build()
                    .encode()
                    .toUriString();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", errorUrl)
                    .build();
        }
    }

    public Map<String, Object> getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri);
        body.add("scope", "openid profile email User.Read User.ReadBasic.All");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://login.microsoftonline.com/common/oauth2/v2.0/token",
                request,
                Map.class);

        return response.getBody();
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://graph.microsoft.com/v1.0/me",
                HttpMethod.GET,
                entity,
                Map.class);
        return response.getBody();
    }

    public String getProfilePicture(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First get the photo metadata to get the URL
            ResponseEntity<Map> metadataResponse = restTemplate.exchange(
                    "https://graph.microsoft.com/v1.0/me/photo",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (metadataResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> metadata = metadataResponse.getBody();
                if (metadata != null && metadata.containsKey("@odata.mediaContentType")) {
                    // Download the actual photo
                    ResponseEntity<byte[]> photoResponse = restTemplate.exchange(
                            "https://graph.microsoft.com/v1.0/me/photo/$value",
                            HttpMethod.GET,
                            entity,
                            byte[].class);

                    if (photoResponse.getStatusCode() == HttpStatus.OK) {
                        // Create uploads/profile directory if it doesn't exist
                        String uploadDir = "uploads/profile";
                        File directory = new File(uploadDir);
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }

                        // Generate unique filename using timestamp and UUID for better uniqueness
                        String filename = "profile_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis()
                                + ".jpg";
                        String filePath = uploadDir + "/" + filename;

                        // Save the photo
                        try (FileOutputStream fos = new FileOutputStream(filePath)) {
                            fos.write(photoResponse.getBody());
                        }

                        // Return the relative path that can be used in the frontend
                        return "profile/" + filename;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching profile picture: " + e.getMessage());
        }
        return null;
    }

    public void removeProfileImage(String url) {
        try {
            String filename = url.substring(url.lastIndexOf("/") + 1);
            String filePath = "src/main/resources" + filename;
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.out.println("Error removing profile picture: " + e.getMessage());
        }
    }

    public void reverseWithArrayList() {
        ArrayList<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);

        for (int i = numbers.size() - 1; i >= 0; i--) {
            System.out.println(numbers.get(i));
        }

    }
}