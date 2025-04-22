package com.example.leave_management.controller;

import com.example.leave_management.dto.UserDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.Employee;
import com.example.leave_management.service.EmployeeService;
import com.example.leave_management.service.MicrosoftAuthService;
import com.example.leave_management.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    @Autowired
    private EmployeeService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MicrosoftAuthService microsoftAuthService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Employee>> register(@Valid @RequestBody UserDto userDto) {
        try {
            ApiResponse<Employee> response = userService.registerNewEmployee(userDto);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception e) {
            throw new AppException("Failed to register user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get current user information", description = "Retrieves the information of the currently authenticated user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User information retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Employee>> getCurrentUser(
            HttpServletRequest request) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(userService.getUserById(UUID.fromString(userId)));
    }

    @GetMapping("/microsoft/login")
    public ResponseEntity<ApiResponse<String>> login() {
        String authUrl = microsoftAuthService.getAuthorizationUrl();
        return ResponseEntity.ok(new ApiResponse<>("Login successful", authUrl, true, HttpStatus.OK, "link"));
    }

    @GetMapping("/microsoft/callback")
    public ResponseEntity<?> callback(@RequestParam(value = "code", required = false) String code) {

        return microsoftAuthService.handleMicrosoftCallback(code);
    }

}