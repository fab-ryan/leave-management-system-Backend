package com.example.leave_management.service.impl;

import com.example.leave_management.dto.LeaveBalanceByDaysDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.*;
import com.example.leave_management.repository.*;
import com.example.leave_management.service.LeaveBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Override
    public ApiResponse<LeaveBalance> getLeaveBalance(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        int currentYear = LocalDate.now().getYear();
        Optional<LeaveBalance> existingBalance = leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear);

        if (existingBalance.isPresent()) {
            return new ApiResponse<>("Leave balance retrieved successfully", existingBalance.get(), true, HttpStatus.OK,
                    "leave_balance");
        } else {
            return initializeLeaveBalance(employeeId);
        }
    }

    @Override
    public ApiResponse<LeaveBalance> updateLeaveBalance(UUID employeeId, LeaveType leaveType, Integer days) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));
        System.out.println("employee:>>>>>> " + employee);
        LeavePolicy policy = employee.getLeavePolicy();
        System.out.println("policy:>>>>>> " + policy);
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee(employee)
                .orElseGet(() -> initializeLeaveBalance(employeeId).getData());
        System.out.println("leaveBalance:>>>>>> " + leaveBalance);
        if (!isLeaveRequestValid(leaveType, leaveBalance, policy)) {
            throw new AppException("Requested leave days exceed available balance or policy limits",
                    HttpStatus.BAD_REQUEST);
        }
        System.out.println("leaveBalance:>>>>>> " + leaveBalance);
        leaveBalance = updateSpecificBalance(leaveBalance, leaveType, days);

        LeaveBalance updatedBalance = leaveBalanceRepository.save(leaveBalance);
        System.out.println("updatedBalance:>>>>>> " + updatedBalance);
        return new ApiResponse<>("Leave balance updated successfully", updatedBalance, true, HttpStatus.OK,
                "leave_balance");
    }

    @Override
    public ApiResponse<LeaveBalance> initializeLeaveBalance(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        int currentYear = LocalDate.now().getYear();
        if (leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear).isPresent()) {
            throw new AppException("Leave balance already exists for this employee", HttpStatus.CONFLICT);
        }

        LeavePolicy policy = employee.getLeavePolicy();
        if (policy == null) {
            policy = leavePolicyRepository.findFirstByOrderByCreatedAtAsc();
            employee.setLeavePolicy(policy);
            employeeRepository.save(employee);
            policy = employee.getLeavePolicy();
        }

        // Initialize balance with policy defaults
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(employee);
        leaveBalance.setYear(currentYear);

        // Calculate carry forward from previous year
        int carryForwardAmount = 0;
        if (currentYear > 2024) { // Only carry forward from 2024 onwards
            Optional<LeaveBalance> previousYearBalance = leaveBalanceRepository
                    .findByEmployeeAndYear(employee, currentYear - 1);
            if (previousYearBalance.isPresent()) {
                carryForwardAmount = calculateCarryForwardAmount(previousYearBalance.get(), policy);
            }
        }

        // Set initial balances with carry forward
        leaveBalance.setAnnualBalance(policy.getAnnualAllowance() + carryForwardAmount);
        leaveBalance.setPersonalBalance(policy.getPersonalAllowance());
        leaveBalance.setSickBalance(policy.getSickAllowance());
        leaveBalance.setMaternityBalance(policy.getMaternityAllowance());
        leaveBalance.setPaternityBalance(policy.getPaternityAllowance());
        leaveBalance.setUnpaidBalance(policy.getUnpaidAllowance());
        leaveBalance.setOtherBalance(policy.getOtherAllowance());
        leaveBalance.setCarryForwardBalance(carryForwardAmount);

        LeaveBalance savedBalance = leaveBalanceRepository.save(leaveBalance);
        return new ApiResponse<>("Leave balance initialized successfully", savedBalance, true, HttpStatus.CREATED,
                "leave_balance");
    }

    @Override
    public ApiResponse<LeaveBalance> processMonthlyAccrual(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        int currentYear = LocalDate.now().getYear();
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear)
                .orElseGet(() -> initializeLeaveBalance(employeeId).getData());

        LeavePolicy policy = employee.getLeavePolicy();
        if (policy == null) {
            throw new AppException("No leave policy found for employee", HttpStatus.BAD_REQUEST);
        }

        // Calculate monthly accrual (1.66 days per month)
        double monthlyAccrual = 1.66;
        int currentAnnualBalance = leaveBalance.getAnnualBalance();
        int newAnnualBalance = currentAnnualBalance + (int) monthlyAccrual;

        // Ensure we don't exceed the policy limit
        int maxAnnualBalance = policy.getAnnualAllowance() + policy.getCarryForwardLimit();
        if (newAnnualBalance > maxAnnualBalance) {
            newAnnualBalance = maxAnnualBalance;
        }

        leaveBalance.setAnnualBalance(newAnnualBalance);
        LeaveBalance updatedBalance = leaveBalanceRepository.save(leaveBalance);

        return new ApiResponse<>("Monthly accrual processed successfully", updatedBalance, true, HttpStatus.OK,
                "leave_balance");
    }

    @Scheduled(cron = "0 0 0 L * ?")
    public void runAutoAccrual() {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee employee : employees) {
            try {
                processMonthlyAccrual(employee.getId());
            } catch (Exception e) {
                // Log the error but continue with other employees
                System.err.println(
                        "Failed to process monthly accrual for employee " + employee.getId() + ": " + e.getMessage());
            }
        }
    }

    private int calculateCarryForwardAmount(LeaveBalance prevBalance, LeavePolicy policy) {
        int unusedAnnualLeave = prevBalance.getAnnualBalance();
        int carryForwardLimit = policy.getCarryForwardLimit();

        // Get holidays in the previous year
        LocalDate yearStart = LocalDate.of(prevBalance.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(prevBalance.getYear(), 12, 31);
        List<Holiday> holidays = holidayRepository.findByDateBetween(yearStart, yearEnd);

        // Adjust carry forward based on holidays
        int holidayCount = holidays.size();
        int adjustedCarryForwardLimit = Math.max(0, carryForwardLimit - holidayCount);

        // Calculate carry forward amount (max of 5 days)
        int carryForwardAmount = Math.min(unusedAnnualLeave, adjustedCarryForwardLimit);
        return Math.min(carryForwardAmount, 5); // Ensure max of 5 days
    }

    public int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<Holiday> holidays = holidayRepository.findByDateBetween(startDate, endDate);

        // Count weekends
        long weekends = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek().getValue() >= 6) { // Saturday or Sunday
                weekends++;
            }
            current = current.plusDays(1);
        }

        return (int) (totalDays - weekends - holidays.size());
    }

    @Override
    public ApiResponse<Boolean> validateLeaveBalance(UUID employeeId, LeaveType leaveType) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        int currentYear = LocalDate.now().getYear();
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear)
                .orElseGet(() -> initializeLeaveBalance(employeeId).getData());

        LeavePolicy policy = employee.getLeavePolicy();

        boolean isValid = isLeaveRequestValid(leaveType, leaveBalance, policy);

        if (!isValid) {
            return new ApiResponse<>("Leave request exceeds available balance or policy limits", false, false,
                    HttpStatus.BAD_REQUEST, "leave_validation");
        }

        return new ApiResponse<>("Leave request is valid", true, true, HttpStatus.OK, "leave_validation");
    }

    @Override
    public ApiResponse<LeaveBalanceByDaysDto> validateLeaveBalanceByDays(UUID employeeId, LeaveType leaveType,
            Integer days) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        int currentYear = LocalDate.now().getYear();
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear)
                .orElseGet(() -> initializeLeaveBalance(employeeId).getData());

        LeavePolicy policy = employee.getLeavePolicy();

        boolean isValid = isLeaveRequestValidByDate(leaveType, policy, leaveBalance, days);
        LeaveBalanceByDaysDto balances = new LeaveBalanceByDaysDto(isValid, getBalance(leaveType, leaveBalance));

        if (!isValid) {
            return new ApiResponse<>("Leave request is invalid due to insufficient balance or exceeding policy limits.",
                    balances, false, HttpStatus.BAD_REQUEST, "leave_validation");
        }

        return new ApiResponse<>("Leave request is valid for the specified number of days.",
                balances, true, HttpStatus.OK, "leave_validation");
    }

    private boolean isLeaveRequestValid(LeaveType leaveType, LeaveBalance balance, LeavePolicy policy) {
        if (policy == null) {
            return false;
        }
        switch (leaveType) {
            case ANNUAL:
                return balance.getAnnualBalance() > 0 || balance.getAnnualBalance() < policy.getAnnualAllowance();
            case SICK:
                return balance.getSickBalance() > 0 || balance.getSickBalance() < policy.getSickAllowance();
            case MATERNITY:
                return balance.getMaternityBalance() > 0
                        || balance.getMaternityBalance() < policy.getMaternityAllowance();
            case PATERNITY:
                return balance.getPaternityBalance() > 0
                        || balance.getPaternityBalance() < policy.getPaternityAllowance();
            case UNPAID:
                return balance.getUnpaidBalance() > 0 || balance.getUnpaidBalance() < policy.getUnpaidAllowance();
            case OTHER:
                return balance.getOtherBalance() > 0 || balance.getOtherBalance() < policy.getOtherAllowance();
            case PERSONAL:
                return balance.getPersonalBalance() > 0 || balance.getPersonalBalance() < policy.getPersonalAllowance();
            default:
                return false;
        }
    }

    public boolean isLeaveRequestValidByDate(LeaveType leaveType, LeavePolicy policy, LeaveBalance balance,
            Integer days) {
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("Invalid number of days");
        }
        if (policy == null) {
            return false;
        }
        switch (leaveType) {
            case ANNUAL:
                return days <= policy.getAnnualAllowance() && days <= balance.getAnnualBalance();
            case SICK:
                return days <= policy.getSickAllowance() && days <= balance.getSickBalance();
            case MATERNITY:
                return days <= policy.getMaternityAllowance() && days <= balance.getMaternityBalance();
            case PATERNITY:
                return days <= policy.getPaternityAllowance() && days <= balance.getPaternityBalance();
            case UNPAID:
                return days <= policy.getUnpaidAllowance() && days <= balance.getUnpaidBalance();
            case OTHER:
                return days <= policy.getOtherAllowance() && days <= balance.getOtherBalance();
            case PERSONAL:
                return days <= policy.getPersonalAllowance() && days <= balance.getPersonalBalance();
            default:
                return false;
        }
    }

    private Integer getBalance(LeaveType leaveType, LeaveBalance balance) {
        switch (leaveType) {
            case ANNUAL:
                return balance.getAnnualBalance();
            case SICK:
                return balance.getSickBalance();
            case MATERNITY:
                return balance.getMaternityBalance();
            case PATERNITY:
                return balance.getPaternityBalance();
            case UNPAID:
                return balance.getUnpaidBalance();
            case OTHER:
                return balance.getOtherBalance();
            case PERSONAL:
                return balance.getPersonalBalance();
            default:
                return 0;
        }
    }

    private LeaveBalance updateSpecificBalance(LeaveBalance balance, LeaveType leaveType, Integer days) {
        switch (leaveType) {
            case ANNUAL:
                balance.setAnnualBalance(balance.getAnnualBalance() - days);
                break;
            case SICK:
                balance.setSickBalance(balance.getSickBalance() - days);
                break;
            case MATERNITY:
                balance.setMaternityBalance(balance.getMaternityBalance() - days);
                break;
            case PATERNITY:
                balance.setPaternityBalance(balance.getPaternityBalance() - days);
                break;
            case UNPAID:
                balance.setUnpaidBalance(balance.getUnpaidBalance() - days);
                break;
            case OTHER:
                balance.setOtherBalance(balance.getOtherBalance() - days);
                break;
            case PERSONAL:
                balance.setPersonalBalance(balance.getPersonalBalance() - days);
                break;
        }
        return balance;
    }

    @Override
    public ApiResponse<List<LeaveBalance>> getEmployeeLeaveHistory(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        List<LeaveBalance> leaveHistory = leaveBalanceRepository.findByEmployeeOrderByYearDesc(employee);
        return new ApiResponse<>("Leave history retrieved successfully", leaveHistory, true, HttpStatus.OK,
                "leave_history");
    }

    @Override
    public ApiResponse<List<LeaveBalance>> getAllLeaveBalances() {
        List<LeaveBalance> leaveBalances = leaveBalanceRepository.findAll();
        return new ApiResponse<>("All leave balances retrieved successfully", leaveBalances, true, HttpStatus.OK,
                "leave_balances");
    }

    @Override
    public ApiResponse<LeaveBalance> approveCompassionDays(UUID employeeId, Integer days) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        int currentYear = LocalDate.now().getYear();
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear)
                .orElseGet(() -> initializeLeaveBalance(employeeId).getData());

        // Update the personal balance (assuming compassion days are deducted from
        // personal leave)
        int currentPersonalBalance = leaveBalance.getPersonalBalance();
        if (currentPersonalBalance < days) {
            throw new AppException("Insufficient personal leave balance for compassion days", HttpStatus.BAD_REQUEST);
        }

        leaveBalance.setPersonalBalance(currentPersonalBalance - days);
        LeaveBalance updatedBalance = leaveBalanceRepository.save(leaveBalance);

        return new ApiResponse<>("Compassion days approved successfully", updatedBalance, true, HttpStatus.OK,
                "leave_balance");
    }
}

class LeaveBalanceByDays {
    Boolean isValid;
    Integer balance;
}