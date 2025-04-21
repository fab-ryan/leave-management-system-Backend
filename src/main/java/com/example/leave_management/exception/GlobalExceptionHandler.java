package com.example.leave_management.exception;

import com.example.leave_management.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AppException.class)
        @Operation(responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - User does not have required role"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error")
        })
        public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
                return ResponseEntity.status(ex.getStatus())
                                .body(new ApiResponse<>(ex.getMessage(), null, false, ex.getStatus(), "error"));
        }

        @ExceptionHandler(ForbiddenException.class)
        @Operation(responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - User does not have required role")
        })
        public ResponseEntity<ApiResponse<?>> handleForbiddenException(ForbiddenException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ApiResponse<>(
                                                "Forbidden - Insufficient permissions",
                                                null,
                                                false,
                                                HttpStatus.FORBIDDEN,
                                                "auth"));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ApiResponse<>(
                                                "Access denied. You don't have permission to perform this action.",
                                                null, false, HttpStatus.FORBIDDEN, "access_denied"));
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ApiResponse<>("Authentication failed. Please check your credentials.",
                                                null, false, HttpStatus.UNAUTHORIZED, "authentication_error"));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ApiResponse<>("Invalid username or password.",
                                                null, false, HttpStatus.UNAUTHORIZED, "bad_credentials"));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>("Validation failed",
                                                errors, false, HttpStatus.BAD_REQUEST, "validation_error"));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(ex.getMessage(), null, false, HttpStatus.BAD_REQUEST,
                                                "validation"));
        }

        @ExceptionHandler(StaleObjectStateException.class)
        @Operation(responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - The resource was modified by another user")
        })
        public ResponseEntity<ApiResponse<?>> handleStaleObjectStateException(StaleObjectStateException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ApiResponse<>(
                                                "The resource was modified by another user. Please refresh and try again.",
                                                null,
                                                false,
                                                HttpStatus.CONFLICT,
                                                "concurrency"));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ApiResponse<>("An unexpected error occurred. Please try again later.",
                                                null, false, HttpStatus.INTERNAL_SERVER_ERROR, "internal_error"));
        }
}