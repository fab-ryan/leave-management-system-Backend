package com.example.leave_management.controller;

import com.example.leave_management.dto.LeaveBalanceByDaysDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.model.LeaveBalance;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.security.RequiresLogin;
import com.example.leave_management.security.RequiresRole;
import com.example.leave_management.service.LeaveBalanceService;
import com.example.leave_management.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import reactor.netty.http.server.HttpServerRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leave-balances")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Leave Balance Management", description = "APIs for managing employee leave balances")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/validate")
    @RequiresLogin
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Validate leave balance", description = "Validate if an employee has sufficient leave balance")
    public ResponseEntity<ApiResponse<Boolean>> validateLeaveBalance(
            HttpServletRequest request,
            @Parameter(description = "Leave type") @RequestParam LeaveType leaveType) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(leaveBalanceService.validateLeaveBalance(UUID.fromString(userId), leaveType));
    }

    @GetMapping("/validate/days")
    @RequiresLogin
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Validate leave balance", description = "Validate if an employee has sufficient leave balance")
    public ResponseEntity<ApiResponse<LeaveBalanceByDaysDto>> validateLeaveBalanceByDays(
            HttpServletRequest request,
            @Parameter(description = "Leave type") @RequestParam LeaveType leaveType,
            @Parameter(description = "Days request") @RequestParam Integer days) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity
                .ok(leaveBalanceService.validateLeaveBalanceByDays(UUID.fromString(userId), leaveType, days));
    }

    @GetMapping("/")
    @Operation(summary = "Get leave balance", description = "Retrieve the leave balance for a specific employee")
    public ResponseEntity<ApiResponse<LeaveBalance>> getLeaveBalance(
            HttpServletRequest request,
            @Parameter(description = "Employee ID", required = false) @PathVariable(required = false) UUID employeeId) {
        if (employeeId == null) {
            String userId = jwtUtil.getLoggedUserId(request);
            employeeId = UUID.fromString(userId);
        }

        return ResponseEntity.ok(leaveBalanceService.getLeaveBalance(employeeId));
    }

    @GetMapping("/admin")
    @RequiresLogin
    @SecurityRequirement(name = "bearerAuth")
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Get all leave balances", description = "Retrieve all leave balances for all employees")
    public ResponseEntity<ApiResponse<List<LeaveBalance>>> getAllLeaveBalances() {
        return ResponseEntity.ok(leaveBalanceService.getAllLeaveBalances());
    }

    @PostMapping("/{employeeId}/initialize")
    @Operation(summary = "Initialize leave balance", description = "Initialize leave balance for a new employee")
    public ResponseEntity<ApiResponse<LeaveBalance>> initializeLeaveBalance(
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId) {
        return ResponseEntity.ok(leaveBalanceService.initializeLeaveBalance(employeeId));
    }

    @PostMapping("/{employeeId}/accrual")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Process monthly accrual", description = "Process monthly leave accrual for an employee")
    public ResponseEntity<ApiResponse<LeaveBalance>> processMonthlyAccrual(
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId) {
        return ResponseEntity.ok(leaveBalanceService.processMonthlyAccrual(employeeId));
    }

    @PostMapping("/{employeeId}/compassion")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Approve compassion days", description = "Approve compassion days for an employee")
    public ResponseEntity<ApiResponse<LeaveBalance>> approveCompassionDays(
            @Parameter(description = "Employee ID") @PathVariable UUID employeeId,
            @Parameter(description = "Number of compassion days") @RequestParam Integer days) {
        return ResponseEntity.ok(leaveBalanceService.approveCompassionDays(employeeId, days));
    }

    // @PutMapping("/{employeeId}/update")
    // @Operation(summary = "Update leave balance", description = "Update the leave
    // balance for a specific employee and leave type")
    // public ResponseEntity<ApiResponse<LeaveBalance>> updateLeaveBalance(
    // @Parameter(description = "Employee ID") @PathVariable UUID employeeId,
    // @Parameter(description = "Number of days to add/subtract") @RequestParam
    // Integer days) {
    // return ResponseEntity.ok(leaveBalanceService.updateLeaveBalance(employeeId,
    // days));
    // }
}
