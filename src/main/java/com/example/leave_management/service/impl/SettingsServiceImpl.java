package com.example.leave_management.service.impl;

import com.example.leave_management.dto.SettingsDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.Settings;
import com.example.leave_management.repository.SettingsRepository;
import com.example.leave_management.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    // @Override
    // public ApiResponse<Settings> createSettings(SettingsDto settingsDto) {
    // Settings settings = new Settings();
    // settings.setDefaultLeaveDays(settingsDto.getDefaultLeaveDays());
    // settings.setMaxConsecutiveLeaveDays(settingsDto.getMaxConsecutiveLeaveDays());
    // settings.setMinNoticePeriodDays(settingsDto.getMinNoticePeriodDays());
    // settings.setAutoApproveLeave(settingsDto.getAutoApproveLeave());
    // settings.setMaxLeaveRequestsPerMonth(settingsDto.getMaxLeaveRequestsPerMonth());

    // Settings savedSettings = settingsRepository.save(settings);
    // return new ApiResponse<>("Settings created successfully", savedSettings,
    // true, HttpStatus.CREATED, "settings");
    // }

    @Override
    public ApiResponse<Settings> updateSettings(SettingsDto settings) {
        Optional<Settings> setts = settingsRepository.findById(settings.getSettingsId());
        Settings setting = setts.get();
        setting.setUpdatedAt(LocalDateTime.now());
        Settings updatedSettings = settingsRepository.save(setting);
        return new ApiResponse<>("Settings updated successfully", updatedSettings, true, HttpStatus.OK, "settings");
    }

    @Override
    public ApiResponse<Settings> getSettingsById(UUID settingsId) {
        return settingsRepository.findById(settingsId)
                .map(settings -> new ApiResponse<>("Settings retrieved successfully", settings, true, HttpStatus.OK,
                        "settings"))
                .orElse(new ApiResponse<>("Settings not found", null, false, HttpStatus.NOT_FOUND, "settings"));
    }

    @Override
    public ApiResponse<Settings> getDefaultSettings() {
        Settings settings = settingsRepository.findFirstByOrderByCreatedAtAsc();
        if (settings != null) {
            return new ApiResponse<>("Default settings retrieved successfully", settings, true, HttpStatus.OK,
                    "settings");
        } else {
            Settings defaultSettings = new Settings();
            defaultSettings.setDefaultLeaveDays(20);
            defaultSettings.setMaxConsecutiveLeaveDays(10);
            defaultSettings.setMinNoticePeriodDays(30);
            defaultSettings.setAutoApproveLeave(false);
            defaultSettings.setMaxLeaveRequestsPerMonth(5);
            defaultSettings.setCreatedAt(LocalDateTime.now());
            defaultSettings.setUpdatedAt(LocalDateTime.now());
            Settings savedSettings = settingsRepository.save(defaultSettings);
            return new ApiResponse<>("Default settings created successfully", savedSettings, true, HttpStatus.CREATED,
                    "settings");
        }
    }

    @Override
    public ApiResponse<Void> deleteSettings(UUID settingsId) {
        if (!settingsRepository.existsById(settingsId)) {
            throw new AppException("Settings not found", HttpStatus.NOT_FOUND);
        }
        settingsRepository.deleteById(settingsId);
        return new ApiResponse<Void>("Settings deleted successfully", null, true, HttpStatus.ACCEPTED, null);
    }
}