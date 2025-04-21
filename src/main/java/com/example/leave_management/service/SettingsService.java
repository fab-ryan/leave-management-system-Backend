package com.example.leave_management.service;

import com.example.leave_management.dto.SettingsDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Settings;

import jakarta.validation.Valid;

import java.util.UUID;

public interface SettingsService {
    ApiResponse<Settings> updateSettings(SettingsDto settingsDto);

    ApiResponse<Settings> getSettingsById(UUID settingsId);

    ApiResponse<Settings> getDefaultSettings();

    ApiResponse<Void> deleteSettings(UUID settingId);

}