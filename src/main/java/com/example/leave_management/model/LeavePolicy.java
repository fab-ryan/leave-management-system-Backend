package com.example.leave_management.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_policies")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Integer annualAllowance;

    @Column(nullable = false)
    private Integer sickAllowance;

    @Column(nullable = false)
    private Integer personalAllowance;

    @Column(nullable = false)
    private Integer carryForwardLimit;

    @Column(nullable = false)
    private Boolean requiresApproval;

    @Column(nullable = false)
    private Boolean requiresDocumentation;

    @Column(nullable = false)
    private Integer minDaysBeforeRequest;
    @Column(name = "maternity_allowance", nullable = false)
    private Integer maternityAllowance = 0;

    @Column(name = "paternity_allowance", nullable = false)
    private Integer paternityAllowance = 0;

    @Column(name = "unpaid_allowance", nullable = false)
    private Integer unpaidAllowance = 0;

    private Integer otherAllowance;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getAnnualAllowance() {
        return annualAllowance;
    }

    public void setAnnualAllowance(Integer annualAllowance) {
        this.annualAllowance = annualAllowance;
    }

    public Integer getSickAllowance() {
        return sickAllowance;
    }

    public void setSickAllowance(Integer sickAllowance) {
        this.sickAllowance = sickAllowance;
    }

    public Integer getPersonalAllowance() {
        return personalAllowance;
    }

    public void setPersonalAllowance(Integer personalAllowance) {
        this.personalAllowance = personalAllowance;
    }

    public Integer getCarryForwardLimit() {
        return carryForwardLimit;
    }

    public void setCarryForwardLimit(Integer carryForwardLimit) {
        this.carryForwardLimit = carryForwardLimit;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Boolean getRequiresDocumentation() {
        return requiresDocumentation;
    }

    public void setRequiresDocumentation(Boolean requiresDocumentation) {
        this.requiresDocumentation = requiresDocumentation;
    }

    public Integer getMinDaysBeforeRequest() {
        return minDaysBeforeRequest;
    }

    public void setMinDaysBeforeRequest(Integer minDaysBeforeRequest) {
        this.minDaysBeforeRequest = minDaysBeforeRequest;
    }

    public void setMaternityAllowance(Integer maternityAllowance) {
        this.maternityAllowance = maternityAllowance;
    }

    public void setOtherAllowance(Integer otherAllowance) {
        this.otherAllowance = otherAllowance;
    }

    public void setPaternityAllowance(Integer paternityAllowance) {
        this.paternityAllowance = paternityAllowance;
    }

    public void setUnpaidAllowance(Integer unpaidAllowance) {
        this.unpaidAllowance = unpaidAllowance;
    }

    public Integer getMaternityAllowance() {
        return maternityAllowance;
    }

    public Integer getOtherAllowance() {
        return otherAllowance;
    }

    public Integer getPaternityAllowance() {
        return paternityAllowance;
    }

    public Integer getUnpaidAllowance() {
        return unpaidAllowance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}