package com.example.leave_management.dto;

public class LeaveBalanceByDaysDto {
    Boolean isValid;
    Integer balance;

    public LeaveBalanceByDaysDto(Boolean isValid, Integer balance) {
        this.balance = balance;
        this.isValid = isValid;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getBalance() {
        return balance;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }
}
