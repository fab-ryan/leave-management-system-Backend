package com.example.leave_management.service;

import com.example.leave_management.model.Notification.NotificationType;

public interface EmailService {
    void sendLeaveStatusEmail(String to, String employeeName, NotificationType type, String leaveDetails);

    void sendWelcomeEmail(String to, String employeeName, String temporaryPassword);

    void sendPasswordResetEmail(String to, String employeeName, String resetToken);
}