package com.example.leave_management.service;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.LeavePolicy;
import com.example.leave_management.repository.LeavePolicyRepository;
import com.example.leave_management.service.impl.LeavePolicyServiceImpl;
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
class LeavePolicyServiceTest {

    @Mock
    private LeavePolicyRepository leavePolicyRepository;

    @InjectMocks
    private LeavePolicyServiceImpl leavePolicyService;

    private LeavePolicy testPolicy;

    @BeforeEach
    void setUp() {
        testPolicy = new LeavePolicy();
        testPolicy.setId(UUID.randomUUID());
        testPolicy.setName("Test Policy");
        testPolicy.setDescription("Test policy description");
        testPolicy.setAnnualAllowance(20);
        testPolicy.setSickAllowance(10);
        testPolicy.setMaternityAllowance(90);
        testPolicy.setPaternityAllowance(14);
        testPolicy.setUnpaidAllowance(30);
        testPolicy.setIsActive(true);
        testPolicy.setRequiresApproval(true);
        testPolicy.setRequiresDocumentation(true);
        testPolicy.setMinDaysBeforeRequest(1);
    }

    @Test
    void getDefaultPolicy_ShouldReturnDefaultPolicy() {
        when(leavePolicyRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(testPolicy);

        ApiResponse<LeavePolicy> response = leavePolicyService.getDefaultPolicy();

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertTrue(response.getData().getIsActive());
    }

    @Test
    void createPolicy_ShouldReturnNewPolicy() {
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenReturn(testPolicy);

        ApiResponse<LeavePolicy> response = leavePolicyService.createPolicy(testPolicy);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(testPolicy.getName(), response.getData().getName());
    }

    @Test
    void updatePolicy_ShouldReturnUpdatedPolicy() {
        when(leavePolicyRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPolicy));
        when(leavePolicyRepository.existsById(any(UUID.class))).thenReturn(true);
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenReturn(testPolicy);

        testPolicy.setName("Updated Policy");
        ApiResponse<LeavePolicy> response = leavePolicyService.updatePolicy(testPolicy.getId(), testPolicy);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals("Updated Policy", response.getData().getName());
    }

    @Test
    void getPolicyById_ShouldReturnPolicy() {
        when(leavePolicyRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPolicy));

        ApiResponse<LeavePolicy> response = leavePolicyService.getPolicyById(testPolicy.getId());

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(testPolicy.getId(), response.getData().getId());
    }

    @Test
    void getAllPolicies_ShouldReturnList() {
        List<LeavePolicy> policies = Arrays.asList(testPolicy);
        when(leavePolicyRepository.findAll()).thenReturn(policies);

        ApiResponse<List<LeavePolicy>> response = leavePolicyService.getAllPolicies();

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void updateIsActiveStatus_ShouldUpdateStatus() {
        when(leavePolicyRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPolicy));
        when(leavePolicyRepository.save(any(LeavePolicy.class))).thenAnswer(invocation -> {
            LeavePolicy savedPolicy = invocation.getArgument(0);
            return savedPolicy;
        });

        ApiResponse<LeavePolicy> response = leavePolicyService.updateIsActiveStatus(testPolicy.getId(), false);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertFalse(response.getData().getIsActive());
        verify(leavePolicyRepository).save(any(LeavePolicy.class));
    }

    @Test
    void getDefault_ShouldReturnDefaultPolicy() {
        when(leavePolicyRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(testPolicy);

        LeavePolicy policy = leavePolicyService.getDefault();

        assertNotNull(policy);
        assertTrue(policy.getIsActive());
    }
}