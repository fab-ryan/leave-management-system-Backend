package com.example.leave_management.controller;

import com.example.leave_management.dto.LoginRequest;
import com.example.leave_management.dto.UpdateEmployeeDto;
import com.example.leave_management.dto.UserDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.LeavePolicy;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.security.RequiresLogin;
import com.example.leave_management.security.RequiresRole;
import com.example.leave_management.service.AuthService;
import com.example.leave_management.service.EmployeeService;
import com.example.leave_management.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Employee Management", description = "APIs for managing Employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    private final Path profilePicturesPath = Paths.get("src/main/resources/uploads/profile");

    @PostMapping("/login")
    @Operation(summary = "Authenticate employee", description = "Authenticate an employee with email and password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticate(loginRequest));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    public ResponseEntity<ApiResponse<Employee>> registerUser(@Valid @RequestBody UserDto userDto) {
        ApiResponse<Employee> response = employeeService.registerNewEmployee(userDto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/employees/{userId}")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user", description = "Updates an existing employee's information")
    public ResponseEntity<ApiResponse<Employee>> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody UpdateEmployeeDto userDto) {
        ApiResponse<Employee> response = employeeService.updateUser(userId, userDto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/employees")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination")
    public ResponseEntity<ApiResponse<Page<Employee>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String policy) {
        ApiResponse<Page<Employee>> response = employeeService.getAllUsers(page, size, sortDirection, search, role,
                department, policy);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/employees/export")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Export employees", description = "Exports employees to a CSV file")
    public ResponseEntity<ByteArrayResource> exportEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String policy) {
        return employeeService.exportEmployees(page, size, sortDirection, search, role, department, policy);
    }

    @GetMapping("/employees/leave-policy")
    @RequiresLogin
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Employe Get current Policy")
    public ResponseEntity<ApiResponse<LeavePolicy>> employeeLeavePolicy(HttpServletRequest request) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity
                .ok(employeeService.employeeLeavePolicy(UUID.fromString(userId)));
    }

    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/employees/{userId}/role")
    @Operation(summary = "Change employee role ", description = "Updates an employee's role")
    public ResponseEntity<ApiResponse<Employee>> changeUserRole(
            @Parameter(description = "Employee ID") @PathVariable UUID userId,
            @Parameter(description = "New role") @RequestParam Employee.UserRole newRole) {
        ApiResponse<Employee> response = employeeService.changeUserRole(userId,
                newRole);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // @GetMapping("/employees/role/{role}")
    // @RequiresLogin
    // @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    // @SecurityRequirement(name = "bearerAuth")
    // @Operation(summary = "Get employees by role", description = "Retrieves all
    // employees with a specific role")
    // public ResponseEntity<ApiResponse<List<Employee>>> getUsersByRole(
    // @Parameter(description = "User role") @PathVariable Employee.UserRole role) {
    // ApiResponse<List<Employee>> response = employeeService.getUsersByRole(role);
    // return ResponseEntity.status(response.getStatus()).body(response);
    // }

    @GetMapping("/search")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Search employees", description = "Searches users by  name")
    public ResponseEntity<ApiResponse<List<Employee>>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        ApiResponse<List<Employee>> response = employeeService.searchUsers(searchTerm);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/employees/profile")
    @RequiresLogin
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update profile", description = "Updates a employee's profile information")
    public ResponseEntity<ApiResponse<Employee>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateEmployeeDto userDto) {
        String userId = jwtUtil.getLoggedUserId(request);
        ApiResponse<Employee> response = employeeService.updateProfile(UUID.fromString(userId),
                userDto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/uploads/profile/{filename:.+}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("src/main/resources/uploads/profile").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}