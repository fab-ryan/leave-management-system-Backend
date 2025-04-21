package com.example.leave_management.controller;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Notification;
import com.example.leave_management.service.NotificationService;
import com.example.leave_management.util.JwtUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getEmployeeNotifications(HttpServletRequest request) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(notificationService.getEmployeeNotifications(UUID.fromString(userId)));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(HttpServletRequest request) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(UUID.fromString(userId)));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(HttpServletRequest request,
            @PathVariable UUID notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpServletRequest request) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(notificationService.markAllAsRead(UUID.fromString(userId)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(HttpServletRequest request) {
        String userId = jwtUtil.getLoggedUserId(request);
        return ResponseEntity.ok(notificationService.getUnreadCount(UUID.fromString(userId)));
    }
}