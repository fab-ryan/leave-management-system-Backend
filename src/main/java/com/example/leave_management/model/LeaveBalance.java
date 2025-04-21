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
@Table(name = "leave_balances")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "annual_balance", nullable = false)
    private Integer annualBalance = 0;

    @Column(name = "sick_balance", nullable = false)
    private Integer sickBalance = 0;

    @Column(name = "maternity_balance", nullable = false)
    private Integer maternityBalance = 0;

    @Column(name = "paternity_balance", nullable = false)
    private Integer paternityBalance = 0;

    @Column(name = "unpaid_balance", nullable = false)
    private Integer unpaidBalance = 0;

    @Column(name = "other_balance", nullable = false)
    private Integer otherBalance = 0;

    @Column(name = "carry_forward_balance", nullable = false)
    private Integer carryForwardBalance = 0;

    @Column(name = "personal_balance", nullable = false)
    private Integer personalBalance = 0;

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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getAnnualBalance() {
        return annualBalance;
    }

    public void setAnnualBalance(Integer annualBalance) {
        this.annualBalance = annualBalance;
    }

    public Integer getSickBalance() {
        return sickBalance;
    }

    public void setSickBalance(Integer sickBalance) {
        this.sickBalance = sickBalance;
    }

    public Integer getMaternityBalance() {
        return maternityBalance;
    }

    public void setMaternityBalance(Integer maternityBalance) {
        this.maternityBalance = maternityBalance;
    }

    public Integer getPaternityBalance() {
        return paternityBalance;
    }

    public void setPaternityBalance(Integer paternityBalance) {
        this.paternityBalance = paternityBalance;
    }

    public Integer getUnpaidBalance() {
        return unpaidBalance;
    }

    public void setUnpaidBalance(Integer unpaidBalance) {
        this.unpaidBalance = unpaidBalance;
    }

    public Integer getOtherBalance() {
        return otherBalance;
    }

    public void setOtherBalance(Integer otherBalance) {
        this.otherBalance = otherBalance;
    }

    public void setPersonalBalance(Integer personalBalance) {
        this.personalBalance = personalBalance;
    }

    public Integer getPersonalBalance() {
        return personalBalance;
    }

    public Integer getCarryForwardBalance() {
        return carryForwardBalance;
    }

    public void setCarryForwardBalance(Integer carryForwardBalance) {
        this.carryForwardBalance = carryForwardBalance;
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
