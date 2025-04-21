package com.example.leave_management.controller;

import com.example.leave_management.dto.SettingsDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.model.Settings;
import com.example.leave_management.security.RequiresLogin;
import com.example.leave_management.security.RequiresRole;
import com.example.leave_management.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@Tag(name = "Settings Management", description = "APIs for managing system settings")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    @Operation(summary = "Get system settings", description = "Retrieve current system settings")
    public ResponseEntity<ApiResponse<Settings>> getSettings() {
        ApiResponse<Settings> response = settingsService.getDefaultSettings();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Update system settings", description = "Update system settings. Only accessible by ADMIN role.")
    public ResponseEntity<ApiResponse<Settings>> updateSettings(@Valid @RequestBody SettingsDto settingsDto) {
        ApiResponse<Settings> response = settingsService.updateSettings(settingsDto);
        return ResponseEntity.ok(response);
    }
}