package com.example.leave_management.service.impl;

import com.example.leave_management.model.Notification.NotificationType;
import com.example.leave_management.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendLeaveStatusEmail(String to, String employeeName, NotificationType type, String leaveDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(getLeaveStatusSubject(type));

            Context context = new Context();
            context.setVariable("employeeName", employeeName);
            context.setVariable("leaveDetails", leaveDetails);
            context.setVariable("status", getStatusText(type));

            String htmlContent = templateEngine.process("leave-status-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String employeeName, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Welcome to Leave Management System");

            Context context = new Context();
            context.setVariable("employeeName", employeeName);
            context.setVariable("temporaryPassword", temporaryPassword);

            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String employeeName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Password Reset Request");

            Context context = new Context();
            context.setVariable("employeeName", employeeName);
            context.setVariable("resetToken", resetToken);

            String htmlContent = templateEngine.process("password-reset-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String getLeaveStatusSubject(NotificationType type) {
        return switch (type) {
            case LEAVE_APPROVED -> "Your Leave Application Has Been Approved";
            case LEAVE_REJECTED -> "Your Leave Application Has Been Rejected";
            case LEAVE_PENDING -> "Leave Application Pending Approval";
            default -> "Leave Application Update";
        };
    }

    private String getStatusText(NotificationType type) {
        return switch (type) {
            case LEAVE_APPROVED -> "approved";
            case LEAVE_REJECTED -> "rejected";
            case LEAVE_PENDING -> "pending";
            default -> "updated";
        };
    }
}