package com.example.leave_management.service.impl;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.*;
import com.example.leave_management.service.LeaveCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeaveCalendarServiceImpl implements LeaveCalendarService {

    // @Override
    // public ApiResponse<Boolean> syncWithOutlook(UUID employeeId) {
    // // TODO:
    // Employee employee = employeeRepository.findById(employeeId).orElseThrow(
    // () -> new RuntimeException("Employee not found"));
    // try {
    // // Get employee's leave applications
    // List<LeaveApplication> leaveApplications =
    // leaveApplicationRepository.findByEmployeeId(employeeId);

    // // Get employee's email from the employee object
    // String employeeEmail = employee.getEmail();

    // // Initialize Microsoft Graph client
    // GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
    // .authenticationProvider(new TokenCredentialAuthProvider(
    // Collections.singletonList("https://graph.microsoft.com/.default")))
    // .buildClient();

    // // Get user's calendar
    // Calendar calendar = graphClient.users(employeeEmail)
    // .calendar()
    // .buildRequest()
    // .get();

    // // Delete existing leave events from calendar
    // List<Event> existingEvents = graphClient.users(employeeEmail)
    // .calendar()
    // .events()
    // .buildRequest()
    // .get()
    // .getCurrentPage();

    // for (Event event : existingEvents) {
    // if (event.getSubject().contains("Leave")) {
    // graphClient.users(employeeEmail)
    // .calendar()
    // .events(event.getId())
    // .buildRequest()
    // .delete();
    // }
    // }

    // // Add new leave events to calendar
    // for (LeaveApplication leave : leaveApplications) {
    // if (leave.getStatus() == LeaveStatus.APPROVED) {
    // Event newEvent = new Event();
    // newEvent.subject = "Leave: " + leave.getLeaveType().toString();
    // newEvent.start = new DateTimeTimeZone();
    // newEvent.start.dateTime = leave.getStartDate().atStartOfDay().toString();
    // newEvent.start.timeZone = "UTC";
    // newEvent.end = new DateTimeTimeZone();
    // newEvent.end.dateTime = leave.getEndDate().atStartOfDay().toString();
    // newEvent.end.timeZone = "UTC";
    // newEvent.isAllDay = true;

    // graphClient.users(employeeEmail)
    // .calendar()
    // .events()
    // .buildRequest()
    // .post(newEvent);
    // }
    // }

    // return new ApiResponse<>("Outlook calendar synced successfully", true, true,
    // HttpStatus.OK,
    // "outlook_sync");

    // } catch (Exception e) {
    // throw new AppException("Failed to sync with Outlook calendar: " +
    // e.getMessage(),
    // HttpStatus.INTERNAL_SERVER_ERROR);
    // }

    // return new ApiResponse<>("Outlook sync not implemented yet", false, false,
    // HttpStatus.NOT_IMPLEMENTED,
    // "outlook_sync");
    // }

}