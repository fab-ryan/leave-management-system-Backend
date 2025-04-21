package com.example.leave_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.leave_management.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/")
@Tag(name = "Home Controller", description = "APIs for home and welcome messages")
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Get home message", description = "Returns a detailed welcome message with response structure")
    public ResponseEntity<ApiResponse<String>> home() {
        return ResponseEntity.ok(new ApiResponse<>("Welcome to Leave Management System API!",
                "Welcome to Leave Management System API!", true, HttpStatus.OK, "data"));
    }
}