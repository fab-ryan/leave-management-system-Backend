package com.example.leave_management.service;

import com.example.leave_management.dto.DepartmentDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Department;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    ApiResponse<Department> createDepartment(DepartmentDto departmentDto);

    ApiResponse<DepartmentDto> updateDepartment(UUID id, DepartmentDto departmentDto);

    ApiResponse<DepartmentDto> getDepartmentById(UUID id);

    ApiResponse<List<DepartmentDto>> getAllDepartments();

    ApiResponse<Void> deleteDepartment(UUID id);

    ApiResponse<DepartmentDto> getDepartmentByName(String name);

    ApiResponse<DepartmentDto> updateStatus(UUID id);
}