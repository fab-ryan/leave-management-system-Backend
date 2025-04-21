package com.example.leave_management.service;

import java.util.UUID;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.dto.response.ManagerDashboardStats;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface DashboardService {

    ApiResponse<Object> getEmployeeDashboard(UUID employeeId);

    ApiResponse<ManagerDashboardStats> getManagerDashboardStats(String department);

    ResponseEntity<byte[]> exportDashboardData(String format, LocalDate startDate, LocalDate endDate);
}
