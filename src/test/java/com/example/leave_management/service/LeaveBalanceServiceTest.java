package com.example.leave_management.service;

import com.example.leave_management.dto.CompassiionRequestDto;
import com.example.leave_management.dto.LeaveBalanceByDaysDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.model.CompassionRequest;
import com.example.leave_management.model.CompassionRequestStatus;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.LeaveBalance;
import com.example.leave_management.repository.CompassionRequestRepository;
import com.example.leave_management.repository.EmployeeRepository;
import com.example.leave_management.repository.LeaveBalanceRepository;
import com.example.leave_management.service.impl.LeaveBalanceServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CompassionRequestRepository compassionRequestRepository;

    @InjectMocks
    private LeaveBalanceServiceImpl leaveBalanceService;

    private Employee testEmployee;
    private LeaveBalance testLeaveBalance;
    private CompassionRequest testCompassionRequest;
    private CompassiionRequestDto testCompassionRequestDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setEmail("test@example.com");
        testEmployee.setName("Test User");

        testLeaveBalance = new LeaveBalance();
        testLeaveBalance.setId(UUID.randomUUID());
        testLeaveBalance.setEmployee(testEmployee);
        testLeaveBalance.setAnnualBalance(20);
        testLeaveBalance.setSickBalance(5);
        testLeaveBalance.setMaternityBalance(15);
        testLeaveBalance.setPaternityBalance(10);
        testLeaveBalance.setCarryForwardBalance(0);

        testCompassionRequest = new CompassionRequest();
        testCompassionRequest.setId(UUID.randomUUID());
        testCompassionRequest.setEmployee(testEmployee);
        testCompassionRequest.setStatus(CompassionRequestStatus.PENDING);
        testCompassionRequest.setWorkDate(LocalDate.now());
        testCompassionRequest.setHoliday(false);

        testCompassionRequestDto = new CompassiionRequestDto();
        testCompassionRequestDto.setWorkDate(LocalDate.now());
        testCompassionRequestDto.setHoliday(false);
        testCompassionRequestDto.setReason("Test reason");
    }

    @Test
    void getLeaveBalance_ShouldReturnBalance() {
        when(leaveBalanceRepository.findByEmployeeId(any(UUID.class))).thenReturn(Optional.of(testLeaveBalance));

        ApiResponse<LeaveBalance> response = leaveBalanceService.getLeaveBalance(testEmployee.getId());

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(testLeaveBalance.getId(), response.getData().getId());
    }

    @Test
    void modifyLeaveBalance_ShouldReturnUpdatedBalance() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        ApiResponse<LeaveBalance> response = leaveBalanceService.modifyLeaveBalance(testEmployee.getId(),
                testLeaveBalance);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
    }

    @Test
    void updateLeaveBalance_ShouldReturnUpdatedBalance() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        ApiResponse<LeaveBalance> response = leaveBalanceService.updateLeaveBalance(testEmployee.getId(),
                LeaveType.ANNUAL, 5);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
    }

    @Test
    void initializeLeaveBalance_ShouldReturnNewBalance() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        ApiResponse<LeaveBalance> response = leaveBalanceService.initializeLeaveBalance(testEmployee.getId());

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
    }

    @Test
    void getEmployeeLeaveHistory_ShouldReturnList() {
        List<LeaveBalance> balances = Arrays.asList(testLeaveBalance);
        when(leaveBalanceRepository.findByEmployeeId(any(UUID.class))).thenReturn(Optional.of(testLeaveBalance));

        ApiResponse<List<LeaveBalance>> response = leaveBalanceService.getEmployeeLeaveHistory(testEmployee.getId());

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void validateLeaveBalance_ShouldReturnTrue() {
        when(leaveBalanceRepository.findByEmployeeId(any(UUID.class)))
                .thenReturn(Optional.of(testLeaveBalance));

        ApiResponse<Boolean> response = leaveBalanceService.validateLeaveBalance(testEmployee.getId(),
                LeaveType.ANNUAL);

        assertTrue(response.getSuccess());
        assertTrue(response.getData());
    }

    @Test
    void validateLeaveBalanceByDays_ShouldReturnDto() {
        when(leaveBalanceRepository.findByEmployeeId(any(UUID.class)))
                .thenReturn(Optional.of(testLeaveBalance));

        ApiResponse<LeaveBalanceByDaysDto> response = leaveBalanceService.validateLeaveBalanceByDays(
                testEmployee.getId(), LeaveType.ANNUAL, 5);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
    }

    @Test
    void getAllLeaveBalances_ShouldReturnList() {
        List<LeaveBalance> balances = Arrays.asList(testLeaveBalance);
        when(leaveBalanceRepository.findAll()).thenReturn(balances);

        ApiResponse<List<LeaveBalance>> response = leaveBalanceService.getAllLeaveBalances();

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void processMonthlyAccrual_ShouldReturnUpdatedBalance() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEmployee));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenReturn(testLeaveBalance);

        ApiResponse<LeaveBalance> response = leaveBalanceService.processMonthlyAccrual(testEmployee.getId());

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
    }

    @Test
    void approveCompassionDays_ShouldReturnUpdatedRequest() {
        when(compassionRequestRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCompassionRequest));
        when(compassionRequestRepository.save(any(CompassionRequest.class))).thenReturn(testCompassionRequest);

        ApiResponse<CompassionRequest> response = leaveBalanceService.approveCompassionDays(
                testEmployee.getId(), testCompassionRequest.getId(), CompassionRequestStatus.APPROVED);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(CompassionRequestStatus.APPROVED, response.getData().getStatus());
    }

    @Test
    void applyCompassionDays_ShouldReturnNewRequest() {
        when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEmployee));
        when(compassionRequestRepository.save(any(CompassionRequest.class))).thenReturn(testCompassionRequest);

        ApiResponse<CompassionRequest> response = leaveBalanceService.applyCompassionDays(
                testEmployee.getId(), testCompassionRequestDto);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
    }

    @Test
    void getCompassionRequests_ShouldReturnList() {
        List<CompassionRequest> requests = Arrays.asList(testCompassionRequest);
        when(compassionRequestRepository.findByEmployeeId(any(UUID.class)))
                .thenReturn(requests);

        ApiResponse<List<CompassionRequest>> response = leaveBalanceService.getCompassionRequests(
                testEmployee.getId(), CompassionRequestStatus.PENDING);

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void updateCompassionRequest_ShouldReturnUpdatedRequest() {
        when(compassionRequestRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCompassionRequest));
        when(compassionRequestRepository.save(any(CompassionRequest.class))).thenReturn(testCompassionRequest);

        ApiResponse<CompassionRequest> response = leaveBalanceService.updateCompassionRequest(
                testCompassionRequest.getId(), testEmployee.getId(), CompassionRequestStatus.REJECTED, "Rejected");

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(CompassionRequestStatus.REJECTED, response.getData().getStatus());
    }

    @Test
    void deleteCompassionRequest_ShouldReturnSuccess() {
        when(compassionRequestRepository.findById(any(UUID.class))).thenReturn(Optional.of(testCompassionRequest));
        doNothing().when(compassionRequestRepository).delete(any(CompassionRequest.class));

        ApiResponse<Void> response = leaveBalanceService.deleteCompassionRequest(testCompassionRequest.getId());

        assertTrue(response.getSuccess());
    }

    @Test
    void getCompassionRequestByAdmin_ShouldReturnList() {
        List<CompassionRequest> requests = Arrays.asList(testCompassionRequest);
        when(compassionRequestRepository.findAll()).thenReturn(requests);

        ApiResponse<List<CompassionRequest>> response = leaveBalanceService.getCompassionRequestByAdmin();

        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }
}