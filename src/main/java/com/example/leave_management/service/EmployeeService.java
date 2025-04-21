package com.example.leave_management.service;

import com.example.leave_management.dto.UpdateEmployeeDto;
import com.example.leave_management.dto.UserDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.LeavePolicy;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.model.Employee.UserStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface EmployeeService {
    ApiResponse<Employee> registerNewEmployee(UserDto userDto);

    Optional<Employee> findByEmail(String email);

    ApiResponse<Employee> updateUser(UUID userId, UpdateEmployeeDto userDto);

    ApiResponse<Employee> getUserById(UUID userId);

    ApiResponse<Employee> getUserByEmail(String email);

    ApiResponse<Void> deleteUser(UUID userId);

    ApiResponse<Employee> changeUserStatus(UUID userId, UserStatus newStatus);

    ApiResponse<Employee> changeUserRole(UUID userId, UserRole newRole);

    ApiResponse<List<Employee>> getUsersByRole(UserRole role);

    ApiResponse<List<Employee>> searchUsers(String searchTerm);

    ApiResponse<Employee> updatePassword(UUID userId, String oldPassword, String newPassword);

    ApiResponse<Employee> resetPassword(UUID userId);

    ApiResponse<Employee> updateProfile(UUID userId, UpdateEmployeeDto userDto);

    Map<String, Object> registerWithAuth2Employee(String email, String name, String profilePictureUrl,
            Employee.UserRole role);

    ApiResponse<LeavePolicy> employeeLeavePolicy(UUID employeeId);

    ResponseEntity<?> handleOAuth2Login(String email, String name, String picture, UserRole role);

    ApiResponse<Page<Employee>> getAllUsers(int page, int size, String sortDirection, String search,
            String role, String department, String policy);

    ResponseEntity<ByteArrayResource> exportEmployees(int page, int size, String sortDirection, String search,
            String role, String department, String policy);

}
