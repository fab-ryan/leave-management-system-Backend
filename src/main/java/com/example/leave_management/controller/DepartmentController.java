package com.example.leave_management.controller;

import com.example.leave_management.dto.DepartmentDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Department;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.security.RequiresLogin;
import com.example.leave_management.security.RequiresRole;
import com.example.leave_management.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/departments")
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @PostMapping
    @Operation(summary = "Create a new department", description = "Creates a new department with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Department created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Department with the same name already exists")
    })
    public ResponseEntity<ApiResponse<Department>> createDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        ApiResponse<Department> response = departmentService.createDepartment(departmentDto);
        return ResponseEntity.status(response.getSuccess() ? 201 : 409).body(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @PutMapping("/{id}/status")
    @Operation(summary = "Update a department", description = "Updates an existing department with the provided details")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateStatus(
            @Parameter(description = "Department ID") @PathVariable UUID id) {
        ApiResponse<DepartmentDto> response = departmentService.updateStatus(id);
        return ResponseEntity.status(response.getSuccess() ? 200 : 404).body(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @PutMapping("/{id}")
    @Operation(summary = "Update a department", description = "Updates an existing department with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Department with the same name already exists")
    })
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(
            @Parameter(description = "Department ID") @PathVariable UUID id,
            @Valid @RequestBody DepartmentDto departmentDto) {
        ApiResponse<DepartmentDto> response = departmentService.updateDepartment(id, departmentDto);
        return ResponseEntity.status(response.getSuccess() ? 200 : 404).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all departments", description = "Retrieves all departments")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Departments retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAllDepartments() {
        ApiResponse<List<DepartmentDto>> response = departmentService.getAllDepartments();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a department", description = "Deletes a department by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @Parameter(description = "Department ID") @PathVariable UUID id) {
        ApiResponse<Void> response = departmentService.deleteDepartment(id);
        return ResponseEntity.status(response.getSuccess() ? 200 : 404).body(response);
    }

    @GetMapping("/search")
    @SecurityRequirement(name = "bearerAuth")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Search department by name", description = "Searches for a department by its name")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<DepartmentDto>> getDepartmentByName(
            @Parameter(description = "Department name") @RequestParam String name) {
        ApiResponse<DepartmentDto> response = departmentService.getDepartmentByName(name);
        return ResponseEntity.status(response.getSuccess() ? 200 : 404).body(response);
    }
}