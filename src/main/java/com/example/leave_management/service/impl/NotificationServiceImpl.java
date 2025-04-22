package com.example.leave_management.service.impl;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.Notification;
import com.example.leave_management.model.Notification.NotificationType;
import com.example.leave_management.repository.EmployeeRepository;
import com.example.leave_management.repository.NotificationRepository;
import com.example.leave_management.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public ApiResponse<Notification> createNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);
        // Send WebSocket notification
        messagingTemplate.convertAndSendToUser(
                notification.getEmployee().getId().toString(),
                "/queue/notifications",
                savedNotification);
        return new ApiResponse<>("Notification created successfully", savedNotification, true, HttpStatus.CREATED,
                "notification");
    }

    @Override
    public ApiResponse<List<Notification>> getEmployeeNotifications(UUID employeeId) {
        List<Notification> notifications = notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        return new ApiResponse<>("Notifications retrieved successfully", notifications, true, HttpStatus.OK,
                "notifications");
    }

    @Override
    public ApiResponse<List<Notification>> getUnreadNotifications(UUID employeeId) {
        List<Notification> notifications = notificationRepository
                .findByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(employeeId);
        return new ApiResponse<>("Unread notifications retrieved successfully", notifications, true, HttpStatus.OK,
                "notifications");
    }

    @Override
    public ApiResponse<Notification> markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException("Notification not found", HttpStatus.NOT_FOUND));
        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return new ApiResponse<>("Notification marked as read", updatedNotification, true, HttpStatus.OK,
                "notification");
    }

    @Override
    public ApiResponse<Void> markAllAsRead(UUID employeeId) {
        List<Notification> notifications = notificationRepository
                .findByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(employeeId);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
        return new ApiResponse<>("All notifications marked as read", null, true, HttpStatus.OK, "notifications");
    }

    @Override
    public ApiResponse<Long> getUnreadCount(UUID employeeId) {
        long count = notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);
        return new ApiResponse<>("Unread count retrieved successfully", count, true, HttpStatus.OK, "count");
    }

    @Override
    public void sendLeaveStatusNotification(UUID employeeId, NotificationType type, String leaveDetails) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

        String title = "";
        String message = "";

        switch (type) {
            case LEAVE_APPROVED:
                title = "Leave Application Approved";
                message = "Your leave application has been approved. Details: " + leaveDetails;
                break;
            case LEAVE_REJECTED:
                title = "Leave Application Rejected";
                message = "Your leave application has been rejected. Details: " + leaveDetails;
                break;
            case LEAVE_PENDING:
                title = "Leave Application Pending";
                message = "Your leave application is pending approval. Details: " + leaveDetails;
                break;
            case LEAVE_COMPENSATED:
                title = "Leave Application Compensated";
                message = "Your leave application has been compensated. Details: " + leaveDetails;
                break;
            case LEAVE_APPROVED_COMPENSATED:
                title = "Leave Application Approved Compensated";
                message = "Your leave application has been approved and compensated. Details: " + leaveDetails;
                break;
            case LEAVE_REJECTED_COMPENSATED:
                title = "Leave Application Rejected Compensated";
                message = "Your leave application has been rejected and compensated. Details: " + leaveDetails;
                break;
            case SYSTEM:
                title = "System Notification";
                message = "A system notification has been sent. Details: " + leaveDetails;
                break;
            case LEAVE_PENDING_COMPENSATED:
                title = "Leave Application Pending Compensated";
                message = "Your leave application is pending approval and compensated. Details: " + leaveDetails;
                break;
        }

        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        createNotification(notification);

    }
}