package com.example.leave_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public class SettingsDto {
    @Schema(hidden = true)
    private UUID settingsId;

    @NotNull(message = "Default leave days is required")
    @Min(value = 1, message = "Default leave days must be at least 1")
    private Integer defaultLeaveDays;

    @NotNull(message = "Maximum consecutive leave days is required")
    @Min(value = 1, message = "Maximum consecutive leave days must be at least 1")
    private Integer maxConsecutiveLeaveDays;

    @NotNull(message = "Minimum notice period days is required")
    @Min(value = 1, message = "Minimum notice period days must be at least 1")
    private Integer minNoticePeriodDays;

    @NotNull(message = "Auto approve leave setting is required")
    private Boolean autoApproveLeave;

    @NotNull(message = "Maximum leave requests per month is required")
    @Min(value = 1, message = "Maximum leave requests per month must be at least 1")
    private Integer maxLeaveRequestsPerMonth;

    public void setAutoApproveLeave(Boolean autoApproveLeave) {
        this.autoApproveLeave = autoApproveLeave;
    }

    public void setDefaultLeaveDays(Integer defaultLeaveDays) {
        this.defaultLeaveDays = defaultLeaveDays;
    }

    public void setMaxConsecutiveLeaveDays(Integer maxConsecutiveLeaveDays) {
        this.maxConsecutiveLeaveDays = maxConsecutiveLeaveDays;
    }

    public void setMaxLeaveRequestsPerMonth(Integer maxLeaveRequestsPerMonth) {
        this.maxLeaveRequestsPerMonth = maxLeaveRequestsPerMonth;
    }

    public void setMinNoticePeriodDays(Integer minNoticePeriodDays) {
        this.minNoticePeriodDays = minNoticePeriodDays;
    }

    public void setSettingsId(UUID settingsId) {
        this.settingsId = settingsId;
    }

    public Boolean getAutoApproveLeave() {
        return autoApproveLeave;
    }

    public Integer getDefaultLeaveDays() {
        return defaultLeaveDays;
    }

    public Integer getMaxConsecutiveLeaveDays() {
        return maxConsecutiveLeaveDays;
    }

    public Integer getMaxLeaveRequestsPerMonth() {
        return maxLeaveRequestsPerMonth;
    }

    public Integer getMinNoticePeriodDays() {
        return minNoticePeriodDays;
    }

    public UUID getSettingsId() {
        return settingsId;
    }

}