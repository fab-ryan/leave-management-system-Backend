package com.example.leave_management.service.impl;

import com.example.leave_management.dto.DepartmentDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Department;
import com.example.leave_management.repository.DepartmentRepository;
import com.example.leave_management.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public ApiResponse<Department> createDepartment(DepartmentDto departmentDto) {
        if (departmentRepository.existsByName(departmentDto.getName())) {
            return new ApiResponse<>("Department with this name already exists", null, false, HttpStatus.CONFLICT,
                    "data");
        }

        Department department = new Department();
        department.setName(departmentDto.getName());
        department.setIsPublic(departmentDto.getIsPublic());
        department.setDescription(departmentDto.getDescription());

        Department savedDepartment = departmentRepository.save(department);
        return new ApiResponse<>("Department created successfully", savedDepartment, true, HttpStatus.OK, "data");
    }

    @Override
    @Transactional
    public ApiResponse<DepartmentDto> updateDepartment(UUID id, DepartmentDto departmentDto) {
        return departmentRepository.findById(id)
                .map(department -> {
                    if (!department.getName().equals(departmentDto.getName()) &&
                            departmentRepository.existsByName(departmentDto.getName().toLowerCase())) {
                        return new ApiResponse<DepartmentDto>("Department with this name already exists", null, false,
                                HttpStatus.CONFLICT, "data");
                    }

                    department.setName(departmentDto.getName().toLowerCase());
                    department.setDescription(departmentDto.getDescription());
                    department.setDescription(departmentDto.getDescription());

                    Department updatedDepartment = departmentRepository.save(department);
                    return new ApiResponse<DepartmentDto>("Department updated successfully",
                            convertToDto(updatedDepartment), true, HttpStatus.OK, "department");
                })
                .orElse(new ApiResponse<DepartmentDto>("Department not found", null, false, HttpStatus.NOT_FOUND,
                        "data"));
    }

    @Override
    public ApiResponse<DepartmentDto> getDepartmentById(UUID id) {
        return departmentRepository.findById(id)
                .map(department -> new ApiResponse<DepartmentDto>("Department retrieved successfully",
                        convertToDto(department), true, HttpStatus.OK, "department"))
                .orElse(new ApiResponse<DepartmentDto>("Department not found", null, false, HttpStatus.NOT_FOUND,
                        "data"));
    }

    @Override
    public ApiResponse<DepartmentDto> updateStatus(UUID id) {
        Department department = departmentRepository.findById(id).orElse(null);
        department.setIsPublic(!department.getIsPublic());

        Department updatedDepartment = departmentRepository.save(department);
        return new ApiResponse<DepartmentDto>("Department updated successfully",
                convertToDto(updatedDepartment), true, HttpStatus.OK, "department");
    }

    @Override
    public ApiResponse<List<DepartmentDto>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDto> departmentDtos = departments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ApiResponse<>("Departments retrieved successfully", departmentDtos, true, HttpStatus.OK,
                "departments");
    }

    @Override
    public ApiResponse<Void> deleteDepartment(UUID id) {
        return departmentRepository.findById(id)
                .map(department -> {
                    departmentRepository.delete(department);
                    return new ApiResponse<Void>("Department deleted successfully", null, true, HttpStatus.OK, "data");
                })
                .orElse(new ApiResponse<Void>("Department not found", null, false, HttpStatus.NOT_FOUND, "data"));
    }

    @Override
    public ApiResponse<DepartmentDto> getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .map(department -> new ApiResponse<DepartmentDto>("Department retrieved successfully",
                        convertToDto(department), true, HttpStatus.OK, "data"))
                .orElse(new ApiResponse<DepartmentDto>("Department not found", null, false, HttpStatus.NOT_FOUND,
                        "data"));
    }

    private DepartmentDto convertToDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName().toUpperCase());
        dto.setDescription(department.getDescription());
        dto.setIsPublic(department.getIsPublic());

        return dto;
    }
}