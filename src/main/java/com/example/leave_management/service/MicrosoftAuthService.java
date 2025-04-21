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

import java.util.HashMap;
import java.util.Map;

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

            UserRole role = UserRole.EMPLOYEE;

            ResponseEntity<?> response = employeeService.handleOAuth2Login(email, name, picture, role);

            // Extract token from response
            String token = null;
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

    private String getProfilePicture(String accessToken) {
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
                    return "https://graph.microsoft.com/v1.0/me/photo/$value";
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching profile picture URL: " + e.getMessage());
        }
        return null;
    }
}