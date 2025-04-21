package com.example.leave_management.dto.response;

import com.example.leave_management.model.LeaveBalance;
import com.example.leave_management.model.LeaveApplication.LeaveStatus;
import lombok.Data;

import java.util.Map;

@Data
public class ManagerDashboardStats {
    private Map<String, Map<String, Long>> leaveTypeMonthlyStats;
    private Map<String, Map<String, Long>> departmentLeaveDays;
    private Map<LeaveStatus, Long> statusCounts;
    private Map<LeaveStatus, Double> statusRatios;

    public void setLeaveTypeMonthlyStats(Map<String, Map<String, Long>> leaveTypeMonthlyStats) {
        this.leaveTypeMonthlyStats = leaveTypeMonthlyStats;
    }

    public void setDepartmentLeaveDays(Map<String, Map<String, Long>> departmentLeaveDays) {
        this.departmentLeaveDays = departmentLeaveDays;
    }

    public void setStatusCounts(Map<LeaveStatus, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public void setStatusRatios(Map<LeaveStatus, Double> statusRatios) {
        this.statusRatios = statusRatios;
    }

    public Map<String, Map<String, Long>> getLeaveTypeMonthlyStats() {
        return leaveTypeMonthlyStats;
    }

    public Map<String, Map<String, Long>> getDepartmentLeaveDays() {
        return departmentLeaveDays;
    }

    public Map<LeaveStatus, Long> getStatusCounts() {

        return statusCounts;
    }

    public Map<LeaveStatus, Double> getStatusRatios() {
        return statusRatios;
    }
}