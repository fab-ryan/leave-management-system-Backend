package com.example.leave_management.service;

import com.example.leave_management.dto.LeaveApplicationDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.LeaveApplication;
import com.example.leave_management.model.LeaveApplication.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.UUID;

public interface LeaveApplicationService {

        ApiResponse<LeaveApplication> createLeaveApplication(LeaveApplicationDto leaveApplicationDto, UUID employeeId);

        ApiResponse<LeaveApplication> getLeaveApplicationById(UUID id);

        ApiResponse<List<LeaveApplication>> getAllLeaveApplications();

        ApiResponse<LeaveApplication> updateLeaveApplication(UUID id, LeaveApplicationDto leaveApplicationDto);

        ApiResponse<LeaveApplication> updateLeaveStatus(UUID id, LeaveStatus status, String comment);

        ApiResponse<LeaveApplication> cancelLeaveApplication(UUID id);

        ApiResponse deleteLeaveApplication(UUID id);

        ApiResponse<List<LeaveApplication>> getLeaveApplicationsByEmployee(UUID employeeId);

        ApiResponse<Page<LeaveApplication>> getLeaveApplicationsByStatus(LeaveStatus status, String startDate,
                        String endDate,
                        String search, String leaveType, int page, int size, String sortDirection);

        ApiResponse<LeaveApplication> addDocument(UUID id, String filename);

        ApiResponse<LeaveApplication> removeDocument(UUID id, String filename);

        ApiResponse<List<LeaveApplication>> getLeaveApplicationsByDate(String date, String department, UUID employeeId);

        ApiResponse<Page<LeaveApplication>> getLeaveApplicationsByEmployeeWithFilters(
                        UUID employeeId,
                        LeaveStatus status,
                        String leaveType,
                        String startDate,
                        String endDate,
                        String search,
                        int page,
                        int size,
                        String sortDirection);

        ResponseEntity<ByteArrayResource> exportLeaveApplications(
                        LeaveStatus status,
                        String leaveType,
                        String startDate,
                        String endDate,
                        String search,
                        int page,
                        int size,
                        String sortDirection);

}
