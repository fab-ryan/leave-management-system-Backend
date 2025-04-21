package com.example.leave_management.dto.response;

import com.example.leave_management.model.Employee.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String departmentName;
}