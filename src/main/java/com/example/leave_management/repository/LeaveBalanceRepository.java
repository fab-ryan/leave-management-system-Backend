package com.example.leave_management.repository;

import com.example.leave_management.model.Employee;
import com.example.leave_management.model.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
    Optional<LeaveBalance> findByEmployeeAndYear(Employee employee, Integer year);

    Optional<LeaveBalance> findByEmployee(Employee employee);

    Optional<LeaveBalance> findByEmployeeId(UUID employeeId);

    List<LeaveBalance> findByEmployeeOrderByYearDesc(Employee employee);
}