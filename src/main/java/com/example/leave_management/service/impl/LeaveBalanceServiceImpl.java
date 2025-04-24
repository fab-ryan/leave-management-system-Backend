package com.example.leave_management.service.impl;

import com.example.leave_management.dto.CompassiionRequestDto;
import com.example.leave_management.dto.LeaveBalanceByDaysDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.*;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.model.Notification.NotificationType;
import com.example.leave_management.repository.*;
import com.example.leave_management.service.LeaveBalanceService;
import com.example.leave_management.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private CompassionRequestRepository compassionRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${leave.management.year:2024}")
    private int initialYear;

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
        LeavePolicy policy = employee.getLeavePolicy();
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee(employee)
                .orElseGet(() -> initializeLeaveBalance(employeeId).getData());
        if (!isLeaveRequestValid(leaveType, leaveBalance, policy)) {
            throw new AppException("Requested leave days exceed available balance or policy limits",
                    HttpStatus.BAD_REQUEST);
        }
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
        if (currentYear > initialYear) { // Only carry forward from 2024 onwards
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
        leaveBalance.setCarryForwardBalance(carryForwardAmount == 0 ? 0 : carryForwardAmount);

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
            policy = leavePolicyRepository.findFirstByOrderByCreatedAtAsc();
            employee.setLeavePolicy(policy);
            employeeRepository.save(employee);
            policy = employee.getLeavePolicy();
        }

        // Calculate monthly accrual (1.66 days per month)
        double monthlyAccrual = 1.66;
        int currentAnnualBalance = leaveBalance.getAnnualBalance();
        int newAnnualBalance = currentAnnualBalance + (int) monthlyAccrual;

        int maxAnnualBalance = policy.getAnnualAllowance() + policy.getCarryForwardLimit();
        if (newAnnualBalance > maxAnnualBalance) {
            newAnnualBalance = maxAnnualBalance;
        }
        leaveBalance.setCarryForwardBalance(policy.getCarryForwardLimit());
        leaveBalance.setPersonalBalance(policy.getPersonalAllowance());
        leaveBalance.setSickBalance(policy.getSickAllowance());
        leaveBalance.setMaternityBalance(policy.getMaternityAllowance());
        leaveBalance.setPaternityBalance(policy.getPaternityAllowance());
        leaveBalance.setUnpaidBalance(policy.getUnpaidAllowance());
        leaveBalance.setOtherBalance(policy.getOtherAllowance());
        leaveBalance.setAnnualBalance(newAnnualBalance);
        LeaveBalance updatedBalance = leaveBalanceRepository.save(leaveBalance);

        return new ApiResponse<>("Monthly accrual processed successfully", updatedBalance, true, HttpStatus.OK,
                "leave_balance");
    }

    @Scheduled(cron = "0 0 0 L * ?")
    // @Scheduled(cron = "1 * * * * *") // every minute of 1 second
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

        holidays.stream().filter(
                holiday -> holiday.isRecurring())
                .collect(Collectors.toList());

        int holidayCount = holidays.size();
        int adjustedCarryForwardLimit = Math.max(0, carryForwardLimit - holidayCount);

        int carryForwardAmount = Math.min(unusedAnnualLeave, adjustedCarryForwardLimit);
        return Math.min(carryForwardAmount, 5);
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
    public ApiResponse<CompassionRequest> approveCompassionDays(UUID employeeId, UUID id,
            CompassionRequestStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        int currentYear = LocalDate.now().getYear();
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeAndYear(employee, currentYear)
                .orElseGet(() -> initializeLeaveBalance(employeeId).getData());

        CompassionRequest compassionRequest = compassionRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Compassion request not found", HttpStatus.NOT_FOUND));

        if (!compassionRequest.getEmployee().getId().equals(employeeId)) {
            throw new AppException("Unauthorized access", HttpStatus.FORBIDDEN);
        }
        if (compassionRequest.getStatus() == status) {
            throw new AppException("Compassion request already " + status, HttpStatus.BAD_REQUEST);
        }
        if (status == CompassionRequestStatus.APPROVED) {
            leaveBalance.setPersonalBalance(leaveBalance.getPersonalBalance() + 1);
            leaveBalanceRepository.save(leaveBalance);
        }
        compassionRequest.setStatus(status);
        compassionRequest.setApprovedBy(employee);
        compassionRequest.setApprovedAt(LocalDate.now());
        compassionRequestRepository.save(compassionRequest);

        return new ApiResponse<>("Compassion days " + status + " successfully",
                compassionRequest, true, HttpStatus.OK,
                "leave_balance");
    }

    @Override
    public ApiResponse<CompassionRequest> applyCompassionDays(UUID employeeId,
            CompassiionRequestDto compassionRequestDto) {
        try {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));
            LeaveBalance leaveBalance = leaveBalanceRepository
                    .findByEmployeeAndYear(employee, LocalDate.now().getYear())
                    .orElseGet(() -> initializeLeaveBalance(employeeId).getData());
            if (leaveBalance.getCarryForwardBalance() == 0) {
                throw new AppException("Compassion request limit reached", HttpStatus.BAD_REQUEST);
            }

            CompassionRequest compassionRequest = new CompassionRequest();
            compassionRequest.setEmployee(employee);
            compassionRequest.setWorkDate(compassionRequestDto.getWorkDate());
            compassionRequest.setReason(compassionRequestDto.getReason());
            compassionRequest.setHoliday(compassionRequestDto.isHoliday());
            compassionRequest.setWeekend(compassionRequestDto.isWeekend());
            compassionRequest.setStatus(CompassionRequestStatus.PENDING);

            CompassionRequest savedCompassionRequest = compassionRequestRepository.save(compassionRequest);
            List<Employee> adminEmployees = employeeRepository
                    .findByRoleIn(Arrays.asList(UserRole.ADMIN, UserRole.MANAGER));
            String leaveDetails = String.format("Type: %s, From: %s",
                    "Compassion",
                    savedCompassionRequest.getWorkDate());
            for (Employee adminEmployee : adminEmployees) {
                notificationService.sendLeaveStatusNotification(
                        adminEmployee.getId(),
                        Notification.NotificationType.LEAVE_WAITING_APPROVAL_COMPENSATED,
                        leaveDetails);
            }
            Notification notification = new Notification();
            notification.setEmployee(employee);
            notification.setMessage("Compassion days applied successfully");
            notification.setType(NotificationType.LEAVE_PENDING_COMPENSATED);
            notification.setTitle("Compassion Days Applied");
            notificationRepository.save(notification);

            return new ApiResponse<CompassionRequest>("Compassion days applied successfully", savedCompassionRequest,
                    true,
                    HttpStatus.OK,
                    "leave_balance");

        } catch (Exception e) {
            System.out.println("Error applying compassion days: " + e.getMessage());
            throw new AppException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<List<CompassionRequest>> getCompassionRequests(UUID employeeId,
            CompassionRequestStatus status) {

        List<CompassionRequest> compassionRequests = compassionRequestRepository.findByEmployeeId(employeeId);

        if (status != null) {
            compassionRequests = compassionRequests.stream()
                    .filter(request -> request.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            compassionRequests = compassionRequests.stream()
                    .filter(request -> request.getStatus() == CompassionRequestStatus.PENDING)
                    .collect(Collectors.toList());
        }

        return new ApiResponse<>("Compassion requests retrieved successfully", compassionRequests,
                true,
                HttpStatus.OK,
                "compassion_requests");
    }

    @Override
    public ApiResponse<List<CompassionRequest>> getCompassionRequestByAdmin() {
        List<CompassionRequest> compassionRequests = compassionRequestRepository.findAll();
        return new ApiResponse<>("Compassion requests retrieved successfully", compassionRequests, true,
                HttpStatus.OK,
                "compassion_requests");
    }

    @Override
    public ApiResponse<CompassionRequest> updateCompassionRequest(UUID id, UUID employeeId,
            CompassionRequestStatus status, String rejectionReason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));
        CompassionRequest compassionRequest = compassionRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Compassion request not found", HttpStatus.NOT_FOUND));

        if (status == CompassionRequestStatus.APPROVED) {
            LeaveBalance leaveBalance = leaveBalanceRepository
                    .findByEmployeeAndYear(compassionRequest.getEmployee(), LocalDate.now().getYear())
                    .orElseGet(() -> initializeLeaveBalance(compassionRequest.getEmployee().getId()).getData());
            leaveBalance.setCarryForwardBalance(leaveBalance.getCarryForwardBalance() + 1);
            leaveBalanceRepository.save(leaveBalance);
            compassionRequest.setApprovedBy(employee);
            compassionRequest.setApprovedAt(LocalDate.now());
        }
        if (status == CompassionRequestStatus.REJECTED) {
            compassionRequest.setRejectionReason(rejectionReason);
        }
        compassionRequest.setStatus(status);
        CompassionRequest updatedCompassionRequest = compassionRequestRepository.save(compassionRequest);
        Notification notification = new Notification();
        notification.setEmployee(compassionRequest.getEmployee());
        notification.setTitle("Compassion Request Updated");
        notification.setMessage("Compassion request updated successfully");
        notification.setType(NotificationType.LEAVE_PENDING_COMPENSATED);
        notificationRepository.save(notification);

        return new ApiResponse<>("Compassion request updated successfully", updatedCompassionRequest, true,
                HttpStatus.OK,
                "leave_balance");

    }

    @Override
    public ApiResponse<Void> deleteCompassionRequest(UUID id) {
        compassionRequestRepository.deleteById(id);
        return new ApiResponse<>("Compassion request deleted successfully", null, true, HttpStatus.OK,
                "leave_balance");

    }

    @Override
    public ApiResponse<LeaveBalance> modifyLeaveBalance(UUID employeeId, LeaveBalance leaveBalance) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));
        LeaveBalance existingLeaveBalance = leaveBalanceRepository.findByEmployeeAndYear(employee,
                leaveBalance.getYear())
                .orElseThrow(() -> new AppException("Leave balance not found", HttpStatus.NOT_FOUND));
        if (existingLeaveBalance == null) {
            existingLeaveBalance = initializeLeaveBalance(employeeId).getData();
        }
        existingLeaveBalance.setAnnualBalance(leaveBalance.getAnnualBalance());
        existingLeaveBalance.setPersonalBalance(leaveBalance.getPersonalBalance());
        existingLeaveBalance.setSickBalance(leaveBalance.getSickBalance());
        existingLeaveBalance.setMaternityBalance(leaveBalance.getMaternityBalance());
        existingLeaveBalance.setPaternityBalance(leaveBalance.getPaternityBalance());
        existingLeaveBalance.setUnpaidBalance(leaveBalance.getUnpaidBalance());
        existingLeaveBalance.setOtherBalance(leaveBalance.getOtherBalance());
        existingLeaveBalance.setCarryForwardBalance(leaveBalance.getCarryForwardBalance());

        LeaveBalance updatedLeaveBalance = leaveBalanceRepository.save(existingLeaveBalance);

        return new ApiResponse<>("Leave balance updated successfully",
                updatedLeaveBalance, true, HttpStatus.OK,
                "leave_balance");

    }
}

class LeaveBalanceByDays {
    Boolean isValid;
    Integer balance;
}