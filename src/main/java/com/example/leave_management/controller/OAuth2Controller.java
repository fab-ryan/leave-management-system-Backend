package com.example.leave_management.controller;

import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class OAuth2Controller {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> handleOAuth2Callback(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String picture,
            @RequestParam UserRole role) {
        return employeeService.handleOAuth2Login(email, name, picture, role);
    }
}