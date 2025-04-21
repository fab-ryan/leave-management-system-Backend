package com.example.leave_management.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "settings")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID settingsId;

    @Column(nullable = false)
    private Integer defaultLeaveDays;

    @Column(nullable = false)
    private Integer maxConsecutiveLeaveDays;

    @Column(nullable = false)
    private Integer minNoticePeriodDays;

    @Column(nullable = false)
    private Boolean autoApproveLeave;

    @Column(nullable = false)
    private Integer maxLeaveRequestsPerMonth;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void setAutoApproveLeave(Boolean autoApproveLeave) {
        this.autoApproveLeave = autoApproveLeave;
    }

    public void setDefaultLeaveDays(Integer defaultLeaveDays) {
        this.defaultLeaveDays = defaultLeaveDays;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getAutoApproveLeave() {
        return autoApproveLeave;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}