package com.example.leave_management.service;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.LeavePolicy;

import java.util.List;
import java.util.UUID;

public interface LeavePolicyService {
    ApiResponse<LeavePolicy> getDefaultPolicy();

    ApiResponse<LeavePolicy> createPolicy(LeavePolicy policy);

    ApiResponse<LeavePolicy> updatePolicy(UUID policyId, LeavePolicy policy);

    ApiResponse<LeavePolicy> getPolicyById(UUID policyId);

    ApiResponse<List<LeavePolicy>> getAllPolicies();

    ApiResponse<LeavePolicy> updateIsActiveStatus(UUID policUuid, Boolean status);

    LeavePolicy getDefault();
}