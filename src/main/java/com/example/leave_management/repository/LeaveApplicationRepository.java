package com.example.leave_management.repository;

import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.model.Department;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.LeaveApplication;
import com.example.leave_management.model.LeaveApplication.LeaveStatus;
import com.example.leave_management.model.LeaveBalance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveApplicationRepository
                extends JpaRepository<LeaveApplication, UUID>, JpaSpecificationExecutor<LeaveApplication> {
        List<LeaveApplication> findByEmployee(Employee employee);

        List<LeaveApplication> findByStatus(LeaveStatus status);

        List<LeaveApplication> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

        List<LeaveApplication> findByEmployeeAndStatus(Employee employee, LeaveStatus status);

        List<LeaveApplication> findByEmployeeIdAndStartDateAfter(UUID employeeId, LocalDate startDate);

        long countByEmployeeAndStatus(Employee employee, LeaveStatus status);

        @Query("SELECT COUNT(la) FROM LeaveApplication la WHERE la.employee = :employee AND la.status = :status AND la.startDate BETWEEN :startDate AND :endDate")
        long countLeaveApplicationsByEmployeeAndStatusAndDateRange(
                        @Param("employee") Employee employee,
                        @Param("status") LeaveStatus status,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT la FROM LeaveApplication la WHERE la.employee.department = :department AND la.employee = :employee AND la.status = :status")
        List<LeaveApplication> findLeaveApplicationsByDepartmentAndEmployeeAndStatus(
                        @Param("department") Department department,
                        @Param("employee") Employee employee,
                        @Param("status") LeaveStatus status);

        List<LeaveApplication> findByEmployeeDepartmentAndStatus(Department department, LeaveStatus status);

        long countByStatus(LeaveStatus status);

        @Query("SELECT la FROM LeaveApplication la WHERE la.status = :status AND la.startDate BETWEEN :startDate AND :endDate")
        List<LeaveApplication> findByStatusAndStartDateBetween(
                        @Param("status") LeaveStatus status,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT la FROM LeaveApplication la WHERE la.employee = :employee AND la.status = :status AND la.leaveType = :leaveType")
        List<LeaveApplication> findByEmployeeAndStatusAndLeaveType(
                        @Param("employee") Employee employee,
                        @Param("status") LeaveStatus status,
                        @Param("leaveType") LeaveType leaveType);

}