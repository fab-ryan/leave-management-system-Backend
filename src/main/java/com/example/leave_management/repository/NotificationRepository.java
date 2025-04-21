package com.example.leave_management.repository;

import com.example.leave_management.model.Notification;
import com.example.leave_management.model.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    List<Notification> findByEmployeeIdAndIsReadFalseOrderByCreatedAtDesc(UUID employeeId);

    List<Notification> findByEmployeeIdAndTypeOrderByCreatedAtDesc(UUID employeeId, NotificationType type);

    long countByEmployeeIdAndIsReadFalse(UUID employeeId);
}