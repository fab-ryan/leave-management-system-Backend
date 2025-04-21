package com.example.leave_management.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import com.example.leave_management.model.LeaveApplication.LeaveStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.example.leave_management.enums.LeaveType;

public class LeaveApplicationDto {

    @Schema(hidden = true)
    private UUID id;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be in the present or future")
    private LocalDate endDate;

    @NotNull(message = "Half day status is required")
    private Boolean isHalfDay = false;

    private Boolean isMorning;

    private LeaveType leaveType;

    @NotNull(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    private List<MultipartFile> documents;

    @Schema(hidden = true)
    private List<DocumentDto> setSupportingDocuments;

    private LeaveStatus status;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsHalfDay() {
        return isHalfDay;
    }

    public void setIsHalfDay(Boolean isHalfDay) {
        this.isHalfDay = isHalfDay;
    }

    public Boolean getIsMorning() {
        return isMorning;
    }

    public void setIsMorning(Boolean isMorning) {
        this.isMorning = isMorning;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<MultipartFile> getDocuments() {
        return documents;
    }

    public void setDocuments(List<MultipartFile> documents) {
        this.documents = documents;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public void setSetSupportingDocuments(List<DocumentDto> setSupportingDocuments) {
        this.setSupportingDocuments = setSupportingDocuments;
    }

    public List<DocumentDto> getSetSupportingDocuments() {
        return setSupportingDocuments;
    }
}
