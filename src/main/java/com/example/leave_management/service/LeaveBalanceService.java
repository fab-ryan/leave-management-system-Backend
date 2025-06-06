package com.example.leave_management.service;

import com.example.leave_management.dto.CompassiionRequestDto;
import com.example.leave_management.dto.LeaveBalanceByDaysDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.model.CompassionRequest;
import com.example.leave_management.model.CompassionRequestStatus;
import com.example.leave_management.model.LeaveBalance;

import java.util.List;
import java.util.UUID;

public interface LeaveBalanceService {
    ApiResponse<LeaveBalance> getLeaveBalance(UUID employeeId);

    ApiResponse<LeaveBalance> modifyLeaveBalance(UUID employeeId, LeaveBalance leaveBalance);

    ApiResponse<LeaveBalance> updateLeaveBalance(UUID employeeId, LeaveType leaveType, Integer days);

    ApiResponse<LeaveBalance> initializeLeaveBalance(UUID employeeId);

    ApiResponse<List<LeaveBalance>> getEmployeeLeaveHistory(UUID employeeId);

    ApiResponse<Boolean> validateLeaveBalance(UUID employeeId, LeaveType leaveType);

    ApiResponse<LeaveBalanceByDaysDto> validateLeaveBalanceByDays(UUID employeeId, LeaveType leaveType, Integer days);

    ApiResponse<List<LeaveBalance>> getAllLeaveBalances();

    ApiResponse<LeaveBalance> processMonthlyAccrual(UUID employeeId);

    ApiResponse<CompassionRequest> approveCompassionDays(UUID employeeId, UUID id, CompassionRequestStatus status);

    ApiResponse<CompassionRequest> applyCompassionDays(UUID employeeId, CompassiionRequestDto compassionRequestDto);

    ApiResponse<List<CompassionRequest>> getCompassionRequests(UUID employeeId, CompassionRequestStatus status);

    ApiResponse<CompassionRequest> updateCompassionRequest(UUID id, UUID employeeId, CompassionRequestStatus status,
            String rejectionReason);

    ApiResponse<Void> deleteCompassionRequest(UUID id);

    ApiResponse<List<CompassionRequest>> getCompassionRequestByAdmin();
}
