package com.example.leave_management.repository;

import com.example.leave_management.model.Department;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.model.Employee.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    // Find by email
    Optional<Employee> findByEmail(String email);

    // Find all users by role
    List<Employee> findByRole(UserRole role);

    // Find all users by status
    List<Employee> findByStatus(UserStatus status);

    // Find users by role and status
    List<Employee> findByRoleAndStatus(UserRole role, UserStatus status);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find users by name (first or last name) containing the given string
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:name% ")
    List<Employee> findByNameContaining(@Param("name") String name);

    // Find users who joined after a specific date
    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :date")
    List<Employee> findUsersJoinedAfter(@Param("date") LocalDateTime date);

    // Find by department
    List<Employee> findByDepartment(Department department);

}