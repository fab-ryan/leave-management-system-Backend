package com.example.leave_management.service.impl;

import com.example.leave_management.dto.UpdateEmployeeDto;
import com.example.leave_management.dto.UserDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.Department;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.model.LeavePolicy;
import com.example.leave_management.repository.DepartmentRepository;
import com.example.leave_management.repository.EmployeeRepository;
import com.example.leave_management.repository.LeavePolicyRepository;
import com.example.leave_management.service.EmployeeService;
import com.example.leave_management.service.LeavePolicyService;
import com.example.leave_management.util.JwtUtil;

import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeServiceEmpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private LeavePolicyService leavePolicyService;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public ApiResponse<Employee> registerNewEmployee(UserDto userDto) {
        try {

            if (employeeRepository.existsByEmail(userDto.getEmail())) {
                return new ApiResponse<>("Email already exists", null, false, HttpStatus.BAD_REQUEST, "data");
            }

            Employee user = new Employee();
            mapUserDtoToEntity(userDto, user);
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setStatus(Employee.UserStatus.ACTIVE);

            if (userDto.getDepartmentId() != null) {
                Department departmentOptional = this.departmentRepository.findById(userDto.getDepartmentId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid department"));

                user.setDepartment(departmentOptional);
            }
            employeeRepository.flush();
            Employee savedUser = employeeRepository.saveAndFlush(user);
            return new ApiResponse<>("User registered successfully", savedUser, true, HttpStatus.CREATED, "user");
        } catch (ObjectOptimisticLockingFailureException ex) {
            String errorString = ex.getMessage();
            System.out.println(errorString);
            throw new IllegalStateException(errorString, ex);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Employee> updateUser(UUID userId, UpdateEmployeeDto employeeDto) {
        try {
            Employee existingEmployee = employeeRepository.findById(userId)
                    .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

            // Update fields
            existingEmployee.setName(employeeDto.getName());
            existingEmployee.setPhoneNumber(employeeDto.getPhoneNumber());
            if (employeeDto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(employeeDto.getDepartmentId())
                        .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));
                existingEmployee.setDepartment(department);
            }

            if (employeeDto.getEmergencyPhoneNumber() != null) {
                existingEmployee.setEmergencyPhoneNumber(employeeDto.getEmergencyPhoneNumber());
            }
            if (employeeDto.getTeam() != null) {
                existingEmployee.setTeam(employeeDto.getTeam());
            }
            if (employeeDto.getLocation() != null) {
                existingEmployee.setLocation(employeeDto.getLocation());
            }

            if (employeeDto.getLeavePolicyId() != null) {
                LeavePolicy leavePolicy = leavePolicyRepository.findById(employeeDto.getLeavePolicyId())
                        .orElseThrow(() -> new AppException("Leave policy not found", HttpStatus.NOT_FOUND));
                existingEmployee.setLeavePolicy(leavePolicy);
            }
            if (employeeDto.getStatus() != null) {
                existingEmployee.setStatus(employeeDto.getStatus());
            }

            // existingEmployee.setDepartment(employeeDto.getDepartment());
            // existingEmployee.setEmergencyContact(employeeDto.getEmergencyContact());
            // existingEmployee.setEmergencyPhoneNumber(employeeDto.getEmergencyPhoneNumber());

            // Save and return
            Employee updatedEmployee = employeeRepository.save(existingEmployee);
            return new ApiResponse<>(
                    "Employee updated successfully",
                    updatedEmployee,
                    true,
                    HttpStatus.OK,
                    "employee");
        } catch (StaleObjectStateException e) {
            // If concurrent modification occurs, retry once
            try {
                Employee existingEmployee = employeeRepository.findById(userId)
                        .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

                // Update fields again
                existingEmployee.setName(employeeDto.getName());
                existingEmployee.setPhoneNumber(employeeDto.getPhoneNumber());
                if (employeeDto.getDepartmentId() != null) {
                    Department department = departmentRepository.findById(employeeDto.getDepartmentId())
                            .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));
                    existingEmployee.setDepartment(department);
                }
                if (employeeDto.getEmergencyPhoneNumber() != null) {
                    existingEmployee.setEmergencyPhoneNumber(employeeDto.getEmergencyPhoneNumber());
                }
                if (employeeDto.getTeam() != null) {
                    existingEmployee.setTeam(employeeDto.getTeam());
                }
                if (employeeDto.getLocation() != null) {
                    existingEmployee.setLocation(employeeDto.getLocation());
                }
                if (employeeDto.getLeavePolicyId() != null) {
                    LeavePolicy leavePolicy = leavePolicyRepository.findById(employeeDto.getLeavePolicyId())
                            .orElseThrow(() -> new AppException("Leave policy not found", HttpStatus.NOT_FOUND));
                    existingEmployee.setLeavePolicy(leavePolicy);
                }
                if (employeeDto.getStatus() != null) {
                    existingEmployee.setStatus(employeeDto.getStatus());
                }
                if (employeeDto.getPhoneNumber() != null) {
                    existingEmployee.setPhoneNumber(employeeDto.getPhoneNumber());
                }
                if (employeeDto.getName() != null) {
                    existingEmployee.setName(employeeDto.getName());
                }

                Employee updatedEmployee = employeeRepository.save(existingEmployee);
                return new ApiResponse<>(
                        "Employee updated successfully after retry",
                        updatedEmployee,
                        true,
                        HttpStatus.OK,
                        "employee");
            } catch (StaleObjectStateException ex) {
                throw new AppException(
                        "The employee was modified by another user. Please refresh and try again.",
                        HttpStatus.CONFLICT);
            }
        }
    }

    @Override
    public ApiResponse<Employee> getUserById(UUID userId) {
        return employeeRepository.findById(userId)
                .map(user -> new ApiResponse<>("Employee found", user, true, HttpStatus.OK,
                        "employee"))
                .orElse(new ApiResponse<>("Employee not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    @Override
    public ApiResponse<Employee> getUserByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(user -> new ApiResponse<>("User found", user, true, HttpStatus.OK,
                        "user"))
                .orElse(new ApiResponse<>("User not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    @Override
    public ApiResponse<Page<Employee>> getAllUsers(int page, int size, String sortDirection, String search,
            String role, String department, String policy) {
        try {
            // Create Pageable with sorting
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

            // Create specification for search if provided
            Specification<Employee> spec = (root, query, cb) -> {
                List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
                if (role != null && !role.isEmpty()) {
                    predicates.add(cb.equal(root.get("role"), UserRole.valueOf(role)));
                }
                if (department != null && !department.isEmpty()) {
                    Department departmentEntity = departmentRepository.findById(UUID.fromString(department))
                            .orElseThrow(() -> new AppException("Department not found", HttpStatus.NOT_FOUND));
                    predicates.add(cb.equal(root.get("department"), departmentEntity));
                }
                if (policy != null && !policy.isEmpty()) {
                    LeavePolicy policyEntity = leavePolicyRepository.findById(UUID.fromString(policy))
                            .orElseThrow(() -> new AppException("Leave policy not found", HttpStatus.NOT_FOUND));
                    predicates.add(cb.equal(root.get("leavePolicy"), policyEntity));
                }
                if (search != null && !search.isEmpty()) {
                    String searchPattern = "%" + search.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), searchPattern),
                            cb.like(cb.lower(root.get("email")), searchPattern),
                            cb.like(cb.lower(root.get("role").as(String.class)), searchPattern)));
                }

                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            };

            // Get paginated results
            Page<Employee> employees = employeeRepository.findAll(spec, pageable);

            return new ApiResponse<>(
                    "Users retrieved successfully",
                    employees,
                    true,
                    HttpStatus.OK,
                    "users");
        } catch (Exception e) {
            throw new AppException("Error retrieving users: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> exportEmployees(int page, int size, String sortDirection, String search,
            String role, String department, String policy) {
        List<Employee> employees = getAllUsers(0, 1000, sortDirection, search, role, department, policy).getData()
                .getContent();
        System.out.println("employees.size() " + employees.size());
        if (employees.isEmpty()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv")
                    .body(new ByteArrayResource(new byte[0]));
        }
        String csvContent = convertEmployeesToCsv(employees);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv")
                .body(new ByteArrayResource(csvContent.getBytes()));
    }

    private String convertEmployeesToCsv(List<Employee> employees) {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Name,Email,Role,Department,Policy\n");
        for (Employee employee : employees) {
            csvContent.append(String.format("%s,%s,%s,%s,%s\n", employee.getName(), employee.getEmail(),
                    employee.getRole(), employee.getDepartment().getName(), employee.getLeavePolicy().getName()));
        }
        return csvContent.toString();
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(UUID userId) {
        return employeeRepository.findById(userId)
                .map(user -> {
                    employeeRepository.delete(user);
                    return new ApiResponse<Void>("User deleted successfully", null, true,
                            HttpStatus.OK, "data");
                })
                .orElse(new ApiResponse<>("User not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    @Override
    @Transactional
    public ApiResponse<Employee> changeUserStatus(UUID userId,
            Employee.UserStatus newStatus) {
        return employeeRepository.findById(userId)
                .map(user -> {
                    user.setStatus(newStatus);
                    Employee updatedUser = employeeRepository.save(user);
                    return new ApiResponse<>("User status updated successfully", updatedUser,
                            true, HttpStatus.OK,
                            "user");
                })
                .orElse(new ApiResponse<>("User not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    @Override
    @Transactional
    public ApiResponse<Employee> changeUserRole(UUID userId, Employee.UserRole newRole) {
        return employeeRepository.findById(userId)
                .map(user -> {
                    user.setRole(newRole);
                    Employee updatedUser = employeeRepository.save(user);
                    return new ApiResponse<>("User role updated successfully", updatedUser, true,
                            HttpStatus.OK,
                            "user");
                })
                .orElse(new ApiResponse<>("User not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    // @Override
    // public ApiResponse<List<User>> getUsersByDepartment(String department) {
    // List<User> users = employeeRepository.findByDepartment(department);
    // return new ApiResponse<>("Users retrieved successfully", users, true,
    // HttpStatus.OK, "users");
    // }

    @Override
    public ApiResponse<List<Employee>> getUsersByRole(Employee.UserRole role) {
        List<Employee> users = employeeRepository.findByRole(role);
        return new ApiResponse<>("Users retrieved successfully", users, true,
                HttpStatus.OK, "users");
    }

    @Override
    public ApiResponse<List<Employee>> searchUsers(String searchTerm) {
        List<Employee> users = employeeRepository.findByNameContaining(searchTerm);
        return new ApiResponse<>("Users retrieved successfully", users, true,
                HttpStatus.OK, "users");
    }

    @Override
    @Transactional
    public ApiResponse<Employee> updatePassword(UUID userId, String oldPassword,
            String newPassword) {
        return employeeRepository.findById(userId)
                .map(user -> {
                    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                        return new ApiResponse<Employee>("Incorrect old password", null, false,
                                HttpStatus.BAD_REQUEST,
                                "data");
                    }
                    user.setPassword(passwordEncoder.encode(newPassword));
                    Employee updatedUser = employeeRepository.save(user);
                    return new ApiResponse<>("Password updated successfully", updatedUser, true,
                            HttpStatus.OK, "user");
                })
                .orElse(new ApiResponse<>("User not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    @Override
    @Transactional
    public ApiResponse<Employee> resetPassword(UUID userId) {
        return employeeRepository.findById(userId)
                .map(user -> {
                    String temporaryPassword = generateTemporaryPassword();
                    user.setPassword(passwordEncoder.encode(temporaryPassword));
                    Employee updatedUser = employeeRepository.save(user);
                    return new ApiResponse<>("Password reset successfully", updatedUser, true,
                            HttpStatus.OK, "user");
                })
                .orElse(new ApiResponse<>("User not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    @Override
    @Transactional
    public ApiResponse<Employee> updateProfile(UUID userId, UpdateEmployeeDto userDto) {
        Department department = departmentRepository.findById(userDto.getDepartmentId()).orElseThrow(
                () -> new AppException("Department not found", HttpStatus.NOT_FOUND));
        return employeeRepository.findById(userId)
                .map(user -> {

                    user.setPhoneNumber(userDto.getPhoneNumber());
                    user.setTeam(userDto.getTeam());
                    user.setLocation(userDto.getLocation());
                    user.setProfileCompleted(true);
                    user.setDepartment(department);
                    user.setEmergencyPhoneNumber(userDto.getEmergencyPhoneNumber());

                    Employee updatedUser = employeeRepository.save(user);
                    return new ApiResponse<>("Profile updated successfully", updatedUser, true,
                            HttpStatus.OK, "user");
                })
                .orElse(new ApiResponse<>("User not found", null, false,
                        HttpStatus.NOT_FOUND, "data"));
    }

    private void mapUserDtoToEntity(UserDto userDto, Employee user) {

        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setEmergencyPhoneNumber(userDto.getEmergencyPhoneNumber());
        user.setTeam(userDto.getTeam());
        user.setLocation(userDto.getLocation());
        user.setName(userDto.getName());
        LeavePolicy defaultPolicyResponse = leavePolicyService.getDefault();
        user.setLeavePolicy(defaultPolicyResponse);

    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public Map<String, Object> registerWithAuth2Employee(String email, String name, String profilePictureUrl,
            Employee.UserRole role) {
        try {
            Optional employee = employeeRepository.findByEmail(email);
            if (employee.isPresent()) {
                String token = jwtUtil.generateToken((Employee) employee.get());

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                return response;
            } else {

                Employee user = new Employee();
                String randomPassword = UUID.randomUUID().toString();
                user.setPassword(passwordEncoder.encode(randomPassword));
                user.setStatus(Employee.UserStatus.ACTIVE);
                user.setEmail(email);
                user.setName(name);
                user.setProfilePictureUrl(profilePictureUrl);
                user.setRole(role);

                employeeRepository.flush();
                employeeRepository.saveAndFlush(user);

                String token = jwtUtil.generateToken(user);

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("role", user.getRole());
                return response;
            }

        } catch (Exception e) {
            throw new AppException(
                    "The employee was modified by another user. Please refresh and try again.",
                    HttpStatus.CONFLICT);
        }
    }

    public ApiResponse<LeavePolicy> employeeLeavePolicy(UUID employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        if (employee.getLeavePolicy() == null) {
            LeavePolicy defaultPolicyResponse = leavePolicyService.getDefault();
            employee.setLeavePolicy(defaultPolicyResponse);
            employeeRepository.save(employee);
        }

        LeavePolicy leavePolicy = employee.getLeavePolicy();
        if (leavePolicy == null) {
            LeavePolicy defaultPolicyResponse = leavePolicyService.getDefault();
            employee.setLeavePolicy(defaultPolicyResponse);
            employeeRepository.save(employee);
        }

        return new ApiResponse<>("Employee leave policy retrieved successfully", leavePolicy, true, HttpStatus.OK,
                "leave_policy");

    }

    @Override
    public ResponseEntity<?> handleOAuth2Login(String email, String name, String picture, UserRole role) {
        Optional<Employee> existingEmployee = employeeRepository.findByEmail(email);

        if (existingEmployee.isPresent()) {
            Employee employee = existingEmployee.get();
            if (picture != null) {
                employee.setProfilePictureUrl(picture);

                employeeRepository.save(employee);
            }
            String token = jwtUtil.generateToken(employee);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", employee.getRole());
            return ResponseEntity
                    .ok(new ApiResponse<>("Login successful", response, true, HttpStatus.OK, "access_token"));
        } else {
            // Create new employee
            Employee newEmployee = new Employee();
            newEmployee.setEmail(email);
            newEmployee.setName(name);
            newEmployee.setProfilePictureUrl(picture);
            if (role == UserRole.EMPLOYEE) {
                newEmployee.setRole(UserRole.EMPLOYEE);
            } else {
                newEmployee.setRole(UserRole.ADMIN);
            }
            newEmployee.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newEmployee.setStatus(Employee.UserStatus.ACTIVE);

            Employee savedEmployee = employeeRepository.save(newEmployee);
            String token = jwtUtil.generateToken(savedEmployee);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", savedEmployee.getRole());
            return ResponseEntity
                    .ok(new ApiResponse<>("Account created and logged in", response, true, HttpStatus.OK,
                            "access_token"));
        }
    }
}