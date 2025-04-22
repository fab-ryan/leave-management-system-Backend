package com.example.leave_management.service;

import com.example.leave_management.dto.DepartmentDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Department;
import com.example.leave_management.repository.DepartmentRepository;
import com.example.leave_management.service.impl.DepartmentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department testDepartment;
    private DepartmentDto testDepartmentDto;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(UUID.randomUUID());
        testDepartment.setName("Test Department");
        testDepartment.setDescription("Test department description");
        testDepartment.setIsPublic(true);

        testDepartmentDto = new DepartmentDto();
        testDepartmentDto.setName("Test Department");
        testDepartmentDto.setDescription("Test department description");
    }

    @Test
    void createDepartment_ShouldReturnSuccess() {
        when(departmentRepository.save(any(Department.class))).thenReturn(testDepartment);

        ApiResponse<Department> response = departmentService.createDepartment(testDepartmentDto);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(testDepartment.getName(), response.getData().getName());
    }

    @Test
    void getAllDepartments_ShouldReturnList() {
        List<Department> departments = Arrays.asList(testDepartment);
        when(departmentRepository.findAll()).thenReturn(departments);

        ApiResponse<List<DepartmentDto>> response = departmentService.getAllDepartments();

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void deleteDepartment_ShouldReturnSuccess() {
        when(departmentRepository.findById(any(UUID.class))).thenReturn(Optional.of(testDepartment));
        doNothing().when(departmentRepository).delete(any(Department.class));

        ApiResponse<Void> response = departmentService.deleteDepartment(testDepartment.getId());

        assertTrue(response.getSuccess());
    }

    @Test
    void updateStatus_ShouldToggleStatus() {
        when(departmentRepository.findById(any(UUID.class))).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(testDepartment);

        ApiResponse<DepartmentDto> response = departmentService.updateStatus(testDepartment.getId());

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertFalse(response.getData().getIsPublic());
    }
}