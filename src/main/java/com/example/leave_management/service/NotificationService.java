package com.example.leave_management.service;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Notification;
import com.example.leave_management.model.Notification.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    ApiResponse<Notification> createNotification(Notification notification);

    ApiResponse<List<Notification>> getEmployeeNotifications(UUID employeeId);

    ApiResponse<List<Notification>> getUnreadNotifications(UUID employeeId);

    ApiResponse<Notification> markAsRead(UUID notificationId);

    ApiResponse<Void> markAllAsRead(UUID employeeId);

    ApiResponse<Long> getUnreadCount(UUID employeeId);

    void sendLeaveStatusNotification(UUID employeeId, NotificationType type, String leaveDetails);
}