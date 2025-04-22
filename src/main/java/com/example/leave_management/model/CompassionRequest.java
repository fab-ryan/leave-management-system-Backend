package com.example.leave_management.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "compassion_requests")
public class CompassionRequest {
    @Schema(hidden = true)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompassionRequestStatus status = CompassionRequestStatus.PENDING;

    @Column(nullable = false)
    private boolean isHoliday;

    @Column(nullable = false)
    private boolean isWeekend;

    @Column
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column
    private LocalDate approvedAt;

    public void setApprovedAt(LocalDate approvedAt) {
        this.approvedAt = approvedAt;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setHoliday(boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public void setStatus(CompassionRequestStatus status) {
        this.status = status;
    }

    public void setWeekend(boolean isWeekend) {
        this.isWeekend = isWeekend;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalDate getApprovedAt() {
        return approvedAt;
    }

    public Employee getApprovedBy() {
        return approvedBy;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getReason() {
        return reason;
    }

    public UUID getId() {
        return id;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public CompassionRequestStatus getStatus() {
        return status;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public boolean isWeekend() {
        return isWeekend;
    }
}