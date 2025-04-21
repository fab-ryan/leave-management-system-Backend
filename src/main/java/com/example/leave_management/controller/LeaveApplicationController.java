package com.example.leave_management.controller;

import com.example.leave_management.dto.LeaveApplicationDto;
import com.example.leave_management.dto.DocumentDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.LeaveApplication;
import com.example.leave_management.model.LeaveApplication.LeaveStatus;
import com.example.leave_management.service.FileStorageService;
import com.example.leave_management.service.LeaveApplicationService;
import com.example.leave_management.security.RequiresRole;
import com.example.leave_management.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.security.RequiresLogin;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/leave-applications")
@Tag(name = "Leave Application Management", description = "APIs for managing leave applications")
public class LeaveApplicationController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresLogin
    @Operation(summary = "Create a new leave application with optional supporting documents")
    public ResponseEntity<ApiResponse<LeaveApplication>> createLeaveApplication(
            @Valid @ModelAttribute LeaveApplicationDto leaveApplicationDto,
            HttpServletRequest request) {

        String userId = jwtUtil.getLoggedUserId(request);

        if (leaveApplicationDto.getDocuments() != null && !leaveApplicationDto.getDocuments().isEmpty()) {
            List<DocumentDto> documentDtos = leaveApplicationDto.getDocuments().stream()
                    .map(file -> {
                        String filename = fileStorageService.store(file);
                        String type = file.getContentType().startsWith("image/") ? "image" : "file";
                        return new DocumentDto(type, filename);
                    })
                    .toList();
            leaveApplicationDto.setSetSupportingDocuments(documentDtos);
        }
        return ResponseEntity
                .ok(leaveApplicationService.createLeaveApplication(leaveApplicationDto, UUID.fromString(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave application by ID")
    public ResponseEntity<ApiResponse<LeaveApplication>> getLeaveApplicationById(@PathVariable UUID id) {
        return ResponseEntity.ok(leaveApplicationService.getLeaveApplicationById(id));
    }

    @GetMapping
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Get all leave applications")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getAllLeaveApplications(HttpServletRequest request) {
        return ResponseEntity.ok(leaveApplicationService.getAllLeaveApplications());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a leave application")
    public ResponseEntity<ApiResponse<LeaveApplication>> updateLeaveApplication(
            @PathVariable UUID id,
            @Valid @RequestBody LeaveApplicationDto leaveApplicationDto) {
        return ResponseEntity.ok(leaveApplicationService.updateLeaveApplication(id, leaveApplicationDto));
    }

    @PutMapping("/{id}/status")
    @RequiresLogin
    @RequiresRole({ UserRole.MANAGER, UserRole.ADMIN })
    @Operation(summary = "Update leave application status")
    public ResponseEntity<ApiResponse<LeaveApplication>> updateLeaveStatus(
            @PathVariable UUID id,
            @RequestParam LeaveStatus status,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(leaveApplicationService.updateLeaveStatus(id, status, comment));
    }

    @PutMapping("/{id}/cancel")
    @RequiresLogin
    @Operation(summary = "Cancel a leave application")
    public ResponseEntity<ApiResponse<LeaveApplication>> cancelLeaveApplication(@PathVariable UUID id) {
        return ResponseEntity.ok(leaveApplicationService.cancelLeaveApplication(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a leave application")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveApplication(@PathVariable UUID id) {
        return ResponseEntity.ok(leaveApplicationService.deleteLeaveApplication(id));
    }

    @GetMapping("/employee")
    @RequiresLogin
    @RequiresRole({ UserRole.EMPLOYEE, UserRole.MANAGER, UserRole.ADMIN })
    @Operation(summary = "Get leave applications by employee with optional filters for status and leave type")
    public ResponseEntity<ApiResponse<Page<LeaveApplication>>> getLeaveApplicationsByEmployee(
            HttpServletRequest request,
            @RequestParam(required = false) LeaveStatus status,
            // also by Range of dates
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String leaveType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(
                leaveApplicationService.getLeaveApplicationsByEmployeeWithFilters(UUID.fromString(userId), status,
                        leaveType, startDate, endDate, search, page, size, sortDirection));
    }

    @GetMapping("/status/{status}")
    @RequiresLogin
    @RequiresRole({ UserRole.MANAGER, UserRole.ADMIN, UserRole.EMPLOYEE })
    @Operation(summary = "Get leave applications by status")
    public ResponseEntity<ApiResponse<Page<LeaveApplication>>> getLeaveApplicationsByStatus(
            @PathVariable LeaveStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String leaveType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        return ResponseEntity.ok(leaveApplicationService.getLeaveApplicationsByStatus(status, startDate, endDate,
                search, leaveType, page, size, sortDirection));
    }

    @GetMapping("/export/{status}")
    @RequiresLogin
    @RequiresRole({ UserRole.MANAGER, UserRole.ADMIN })
    @Operation(summary = "Export leave applications to CSV")
    public ResponseEntity<ByteArrayResource> exportLeaveApplications(
            @PathVariable LeaveStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String leaveType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        return leaveApplicationService.exportLeaveApplications(status, leaveType, startDate, endDate, search, page,
                size, sortDirection);
    }

    @GetMapping("/date")
    @RequiresLogin
    @Operation(summary = "Get leave applications by date")
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> getLeaveApplicationsByDate(
            HttpServletRequest request,
            @RequestParam String selectedDate,
            @RequestParam String department) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(leaveApplicationService.getLeaveApplicationsByDate(selectedDate, department,
                UUID.fromString(userId)));
    }

    @PostMapping("/{id}/documents")
    @RequiresLogin
    @RequiresRole({ UserRole.EMPLOYEE })
    @Operation(summary = "Upload supporting documents for a leave application")
    public ResponseEntity<ApiResponse<LeaveApplication>> uploadDocuments(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        String filename = fileStorageService.store(file);
        return ResponseEntity.ok(leaveApplicationService.addDocument(id, filename));
    }

    @GetMapping("/documents/{filename:.+}")
    @RequiresLogin
    @RequiresRole({ UserRole.EMPLOYEE, UserRole.MANAGER, UserRole.ADMIN })
    @Operation(summary = "Download a supporting document")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable String filename) {
        Resource file = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @DeleteMapping("/{id}/documents/{filename:.+}")
    @RequiresLogin
    @RequiresRole({ UserRole.EMPLOYEE })
    @Operation(summary = "Delete a supporting document")
    public ResponseEntity<ApiResponse<LeaveApplication>> deleteDocument(
            @PathVariable UUID id,
            @PathVariable String filename) {
        return ResponseEntity.ok(leaveApplicationService.removeDocument(id, filename));
    }

}
