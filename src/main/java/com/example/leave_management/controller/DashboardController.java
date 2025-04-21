package com.example.leave_management.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.dto.response.ManagerDashboardStats;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.security.RequiresLogin;
import com.example.leave_management.security.RequiresRole;
import com.example.leave_management.service.DashboardService;
import com.example.leave_management.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController(value = "dashboard")
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Dashboard APIs")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("employee")
    @SecurityRequirement(name = "bearerAuth")
    @RequiresLogin
    @Operation(summary = "Employee Dashboard", description = "This endpoint returns the employee dashboard.")
    public ResponseEntity<ApiResponse<Object>> employeeDashboard(
            HttpServletRequest request) {
        String employeeId = jwtUtil.getLoggedUserId(request);

        ApiResponse<Object> response = dashboardService.getEmployeeDashboard(UUID.fromString(employeeId));

        return ResponseEntity.ok(response);

    }

    @GetMapping("manager")
    @SecurityRequirement(name = "bearerAuth")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Manager Dashboard", description = "This endpoint returns the manager dashboard.")
    public ResponseEntity<ApiResponse<ManagerDashboardStats>> managerDashboard(
            HttpServletRequest request,
            @RequestParam(value = "department", required = false) String departmentId) {
        ApiResponse<ManagerDashboardStats> response = dashboardService
                .getManagerDashboardStats(departmentId);
        return ResponseEntity.ok(response);
    }

    @Hidden
    @RequestMapping("/other")
    public ResponseEntity<ApiResponse<Object>> otherMethod(
            HttpServletRequest request) {
        // Implementation here
        return ResponseEntity.ok(null);
    }
}
