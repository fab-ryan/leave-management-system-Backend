package com.example.leave_management.controller;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.model.LeavePolicy;
import com.example.leave_management.security.RequiresLogin;
import com.example.leave_management.security.RequiresRole;
import com.example.leave_management.service.LeavePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leave-policies")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Leave Policy Management", description = "APIs for managing leave policies")
public class LeavePolicyController {
    @Autowired
    private LeavePolicyService leavePolicyService;

    @GetMapping("/")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.EMPLOYEE })
    @Operation(summary = "Get default policy", description = "Retrieve the default leave policy")
    public ResponseEntity<ApiResponse<LeavePolicy>> getDefaultPolicy() {
        return ResponseEntity.ok(leavePolicyService.getDefaultPolicy());
    }

    @GetMapping("/all")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Get all policy", description = "Retrieve the all leave policy")
    public ResponseEntity<ApiResponse<List<LeavePolicy>>> getAllPolicies() {
        return ResponseEntity.ok(leavePolicyService.getAllPolicies());
    }

    @PostMapping
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Create policy", description = "Create a new leave policy")
    public ResponseEntity<ApiResponse<LeavePolicy>> createPolicy(
            @Parameter(description = "Leave policy details") @RequestBody LeavePolicy policy) {
        return ResponseEntity.ok(leavePolicyService.createPolicy(policy));
    }

    @PutMapping("/{policyId}")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Update policy", description = "Update an existing leave policy")
    public ResponseEntity<ApiResponse<LeavePolicy>> updatePolicy(
            @Parameter(description = "Policy ID") @PathVariable UUID policyId,
            @Parameter(description = "Updated policy details") @RequestBody LeavePolicy policy) {
        return ResponseEntity.ok(leavePolicyService.updatePolicy(policyId, policy));
    }

    @PutMapping("/{policyId}/status")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Update policy", description = "Update an existing leave policy")
    public ResponseEntity<ApiResponse<LeavePolicy>> updateStatus(
            @Parameter(description = "Policy ID") @PathVariable UUID policyId,
            @Parameter(description = "Updated Status") @RequestParam Boolean status) {
        return ResponseEntity.ok(leavePolicyService.updateIsActiveStatus(policyId, status));
    }

    @GetMapping("/{policyId}")
    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @Operation(summary = "Get policy by ID", description = "Retrieve a specific leave policy by its ID")
    public ResponseEntity<ApiResponse<LeavePolicy>> getPolicyById(
            @Parameter(description = "Policy ID") @PathVariable UUID policyId) {
        return ResponseEntity.ok(leavePolicyService.getPolicyById(policyId));
    }

}