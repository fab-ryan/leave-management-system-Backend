package com.example.leave_management.service.impl;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.LeavePolicy;
import com.example.leave_management.repository.LeavePolicyRepository;
import com.example.leave_management.service.LeavePolicyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LeavePolicyServiceImpl implements LeavePolicyService {
    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Override
    public ApiResponse<LeavePolicy> getDefaultPolicy() {
        LeavePolicy policy = leavePolicyRepository.findFirstByOrderByCreatedAtAsc();
        if (policy == null) {
            // Create default policy if none exists
            policy = new LeavePolicy();
            policy.setName("Standard Policy");
            policy.setDescription("Standard leave policy with basic entitlements and requirements");
            policy.setIsActive(true);
            policy.setAnnualAllowance(20);
            policy.setSickAllowance(10);
            policy.setPersonalAllowance(3);
            policy.setCarryForwardLimit(5);
            policy.setRequiresApproval(true);
            policy.setRequiresDocumentation(true);
            policy.setMinDaysBeforeRequest(14);
            policy.setAnnualAllowance(20);
            policy.setMaternityAllowance(10);
            policy.setPaternityAllowance(10);
            policy.setUnpaidAllowance(10);
            policy.setOtherAllowance(5);
            policy = leavePolicyRepository.save(policy);
        }
        return new ApiResponse<>("Policy retrieved successfully", policy, true, HttpStatus.OK, "policy");
    }

    @Override
    public ApiResponse<List<LeavePolicy>> getAllPolicies() {
        List<LeavePolicy> policies = leavePolicyRepository.findAll();
        return new ApiResponse<>("Policies retrieved successfully", policies, true, HttpStatus.OK, "policies");
    }

    @Override
    public ApiResponse<LeavePolicy> createPolicy(LeavePolicy policy) {
        LeavePolicy savedPolicy = leavePolicyRepository.save(policy);
        return new ApiResponse<>("Policy created successfully", savedPolicy, true, HttpStatus.CREATED, "policy");
    }

    @Override
    public ApiResponse<LeavePolicy> updatePolicy(UUID policyId, LeavePolicy policy) {
        LeavePolicy existingPolicy = leavePolicyRepository.findById(policyId)
                .orElseThrow(() -> new AppException("Policy not found", HttpStatus.NOT_FOUND));
        if (!leavePolicyRepository.existsById(policyId)) {
            throw new AppException("Policy not found", HttpStatus.NOT_FOUND);
        }
        policy.setId(policyId);
        policy.setIsActive(existingPolicy.getIsActive());
        LeavePolicy updatedPolicy = leavePolicyRepository.save(policy);
        return new ApiResponse<>("Policy updated successfully", updatedPolicy, true, HttpStatus.OK, "policy");
    }

    @Override
    public ApiResponse<LeavePolicy> getPolicyById(UUID policyId) {
        return leavePolicyRepository.findById(policyId)
                .map(policy -> new ApiResponse<>("Policy retrieved successfully", policy, true, HttpStatus.OK,
                        "policy"))
                .orElse(new ApiResponse<>("Policy not found", null, false, HttpStatus.NOT_FOUND, "policy"));
    }

    @Override
    public ApiResponse<LeavePolicy> updateIsActiveStatus(UUID policyId, Boolean status) {
        LeavePolicy policy = leavePolicyRepository.findById(policyId)
                .orElseThrow();
        policy.setIsActive(status);
        return new ApiResponse<>("Policies retrieved successfully", policy, true, HttpStatus.OK, "policy");

    }

    @Override
    public LeavePolicy getDefault() {
        LeavePolicy policy = leavePolicyRepository.findFirstByOrderByCreatedAtAsc();
        return policy;
    }
}