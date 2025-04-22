package com.example.leave_management.service;

import com.example.leave_management.dto.LoginRequest;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.Employee;
import com.example.leave_management.repository.EmployeeRepository;
import com.example.leave_management.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired

    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public ApiResponse<Map<String, Object>> authenticate(LoginRequest loginRequest) {
        Employee employee = employeeRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException("Unauthorized access", HttpStatus.UNAUTHORIZED));

        // Compare the provided password with the stored hashed password
        if (!passwordEncoder.matches(loginRequest.getPassword(), employee.getPassword())) {
            throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", employee.getRole());

        return new ApiResponse("Login successful", response, true, null, "access_token");
    }
}