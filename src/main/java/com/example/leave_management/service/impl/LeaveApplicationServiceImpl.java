package com.example.leave_management.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.example.leave_management.dto.DocumentDto;
import com.example.leave_management.dto.LeaveApplicationDto;
import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.LeaveApplication;
import com.example.leave_management.model.LeaveApplication.LeaveStatus;
import com.example.leave_management.repository.EmployeeRepository;
import com.example.leave_management.repository.LeaveApplicationRepository;
import com.example.leave_management.service.LeaveApplicationService;
import com.example.leave_management.service.EmailService;
import com.example.leave_management.service.FileStorageService;
import com.example.leave_management.service.NotificationService;
import com.example.leave_management.model.Notification;
import com.example.leave_management.service.LeaveBalanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ByteArrayResource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

        @Autowired
        private LeaveApplicationRepository leaveApplicationRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private FileStorageService fileStorageService;

        @Autowired
        private NotificationService notificationService;

        @Autowired
        private LeaveBalanceService leaveBalanceService;

        @Autowired
        private EmailService emailService;

        @Override
        public ApiResponse<LeaveApplication> createLeaveApplication(LeaveApplicationDto leaveApplicationDto,
                        UUID employeeId) {
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));
                List<LeaveApplication> existingLeaveApplication = leaveApplicationRepository
                                .findByEmployeeAndStatusAndLeaveType(employee, LeaveStatus.PENDING,
                                                leaveApplicationDto.getLeaveType());
                if (existingLeaveApplication.size() > 0) {
                        throw new AppException("You already have a pending leave application for this leave type",
                                        HttpStatus.BAD_REQUEST);
                }
                List<LeaveApplication> approvedLeaves = leaveApplicationRepository.findByEmployeeAndStatus(employee,
                                LeaveStatus.APPROVED);
                for (LeaveApplication approvedLeave : approvedLeaves) {
                        if ((leaveApplicationDto.getStartDate().isBefore(approvedLeave.getEndDate()) ||
                                        leaveApplicationDto.getStartDate().equals(approvedLeave.getEndDate())) &&
                                        (leaveApplicationDto.getEndDate().isAfter(approvedLeave.getStartDate()) ||
                                                        leaveApplicationDto.getEndDate()
                                                                        .equals(approvedLeave.getStartDate()))) {
                                throw new AppException(
                                                "You already have an approved leave application that overlaps with the requested dates",
                                                HttpStatus.BAD_REQUEST);
                        }
                }

                LeaveApplication leaveApplication = new LeaveApplication();
                leaveApplication.setEmployee(employee);
                leaveApplication.setLeaveType(leaveApplicationDto.getLeaveType());
                leaveApplication.setStartDate(leaveApplicationDto.getStartDate());
                leaveApplication.setEndDate(leaveApplicationDto.getEndDate());
                leaveApplication.setIsHalfDay(leaveApplicationDto.getIsHalfDay());
                leaveApplication.setIsMorning(leaveApplicationDto.getIsMorning());
                leaveApplication.setReason(leaveApplicationDto.getReason());
                leaveApplication.setSupportingDocuments(leaveApplicationDto.getSetSupportingDocuments());
                leaveApplication.setStatus(LeaveStatus.PENDING);

                LeaveApplication savedApplication = leaveApplicationRepository.save(leaveApplication);

                // Send notification to employee
                String leaveDetails = String.format("Type: %s, From: %s, To: %s",
                                leaveApplicationDto.getLeaveType(),
                                leaveApplicationDto.getStartDate(),
                                leaveApplicationDto.getEndDate());
                notificationService.sendLeaveStatusNotification(
                                employee.getId(),
                                Notification.NotificationType.LEAVE_PENDING,
                                leaveDetails);
                emailService.sendLeaveStatusEmail(employee.getEmail(), employee.getName(),
                                Notification.NotificationType.LEAVE_PENDING, leaveDetails);

                return new ApiResponse<>("Leave application created successfully", savedApplication, true,
                                HttpStatus.CREATED,
                                "leave_application");
        }

        @Override
        public ApiResponse<LeaveApplication> getLeaveApplicationById(UUID id) {
                return leaveApplicationRepository.findById(id)
                                .map(application -> new ApiResponse<>("Leave application found", application, true,
                                                HttpStatus.OK,
                                                "leave_application"))
                                .orElse(new ApiResponse<>("Leave application not found", null, false,
                                                HttpStatus.NOT_FOUND,
                                                "leave_application"));
        }

        @Override
        public ApiResponse<List<LeaveApplication>> getAllLeaveApplications() {
                List<LeaveApplication> applications = leaveApplicationRepository.findAll();
                return new ApiResponse<>("Leave applications retrieved successfully", applications, true, HttpStatus.OK,
                                "leave_applications");
        }

        @Override
        public ApiResponse<LeaveApplication> updateLeaveApplication(UUID id, LeaveApplicationDto leaveApplicationDto) {
                return leaveApplicationRepository.findById(id)
                                .map(application -> {
                                        if (application.getStatus() != LeaveStatus.PENDING) {
                                                throw new AppException("Cannot update a non-pending leave application",
                                                                HttpStatus.BAD_REQUEST);
                                        }
                                        application.setStartDate(leaveApplicationDto.getStartDate());
                                        application.setEndDate(leaveApplicationDto.getEndDate());
                                        application.setIsHalfDay(leaveApplicationDto.getIsHalfDay());
                                        application.setIsMorning(leaveApplicationDto.getIsMorning());
                                        application.setReason(leaveApplicationDto.getReason());
                                        if (!leaveApplicationDto.getDocuments().isEmpty()) {
                                                application.setSupportingDocuments(
                                                                leaveApplicationDto.getSetSupportingDocuments());
                                        }

                                        LeaveApplication updatedApplication = leaveApplicationRepository
                                                        .save(application);
                                        return new ApiResponse<>("Leave application updated successfully",
                                                        updatedApplication, true,
                                                        HttpStatus.OK, "leave_application");
                                })
                                .orElse(new ApiResponse<>("Leave application not found", null, false,
                                                HttpStatus.NOT_FOUND,
                                                "leave_application"));
        }

        @Override
        public ApiResponse<LeaveApplication> updateLeaveStatus(UUID id, LeaveStatus status, String comment) {
                LeaveApplication leaveApplication = leaveApplicationRepository.findById(id)
                                .orElseThrow(() -> new AppException("Leave application not found",
                                                HttpStatus.NOT_FOUND));
                if (leaveApplication.getStatus() != LeaveStatus.PENDING) {
                        throw new AppException("Cannot update a non-pending leave application",
                                        HttpStatus.BAD_REQUEST);
                }
                if (status == LeaveStatus.APPROVED) {
                        leaveApplication.setStatus(status);
                        leaveApplication.setComment(comment);

                        // Calculate the number of days between start and end date
                        long days = ChronoUnit.DAYS.between(leaveApplication.getStartDate(),
                                        leaveApplication.getEndDate()) + 1;

                        // If it's a half day, count as 0.5 days
                        if (leaveApplication.getIsHalfDay()) {
                                days = 1;
                        }

                        // Update the leave balance
                        leaveBalanceService.updateLeaveBalance(
                                        leaveApplication.getEmployee().getId(),
                                        leaveApplication.getLeaveType(),
                                        (int) days);
                }

                leaveApplication.setStatus(status);
                LeaveApplication updatedApplication = leaveApplicationRepository.save(leaveApplication);

                String leaveDetails = String.format("Type: %s, From: %s, To: %s",
                                leaveApplication.getLeaveType(),
                                leaveApplication.getStartDate(),
                                leaveApplication.getEndDate());

                Notification.NotificationType notificationType = switch (status) {
                        case APPROVED -> Notification.NotificationType.LEAVE_APPROVED;
                        case REJECTED -> Notification.NotificationType.LEAVE_REJECTED;
                        default -> Notification.NotificationType.LEAVE_PENDING;
                };

                notificationService.sendLeaveStatusNotification(
                                leaveApplication.getEmployee().getId(),
                                notificationType,
                                leaveDetails);
                emailService.sendLeaveStatusEmail(leaveApplication.getEmployee().getEmail(),
                                leaveApplication.getEmployee().getName(), notificationType, leaveDetails);

                return new ApiResponse<>("Leave application status updated successfully", updatedApplication, true,
                                HttpStatus.OK,
                                "leave_application");
        }

        @Override
        public ApiResponse<LeaveApplication> cancelLeaveApplication(UUID id) {
                return leaveApplicationRepository.findById(id)
                                .map(application -> {
                                        application.setStatus(LeaveStatus.CANCELLED);
                                        return new ApiResponse<>("Leave application cancelled successfully",
                                                        application, true, HttpStatus.OK, "leave_application");
                                })
                                .orElse(new ApiResponse<>("Leave application not found", null, false,
                                                HttpStatus.NOT_FOUND,
                                                "leave_application"));
        }

        @Override
        public ApiResponse deleteLeaveApplication(UUID id) {
                return leaveApplicationRepository.findById(id)
                                .map(application -> {
                                        if (application.getStatus() != LeaveStatus.PENDING) {
                                                throw new AppException("Cannot delete a non-pending leave application",
                                                                HttpStatus.BAD_REQUEST);
                                        }
                                        leaveApplicationRepository.delete(application);
                                        return new ApiResponse<>("Leave application deleted successfully", null, true,
                                                        HttpStatus.OK,
                                                        "leave_application");
                                })
                                .orElse(new ApiResponse<>("Leave application not found", null, false,
                                                HttpStatus.NOT_FOUND,
                                                "leave_application"));
        }

        @Override
        public ApiResponse<List<LeaveApplication>> getLeaveApplicationsByEmployee(UUID employeeId) {
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new AppException("Employee not found", HttpStatus.NOT_FOUND));

                List<LeaveApplication> applications = leaveApplicationRepository.findByEmployee(employee);
                return new ApiResponse<>("Leave applications retrieved successfully", applications, true, HttpStatus.OK,
                                "leave_applications");
        }

        @Override
        public ApiResponse<Page<LeaveApplication>> getLeaveApplicationsByStatus(LeaveStatus status, String startDate,
                        String endDate, String search, String leaveType, int page, int size, String sortDirection) {

                Sort.Direction direction = Sort.Direction.fromString(sortDirection);
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

                Specification<LeaveApplication> spec = (root, query, cb) -> {
                        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
                        if (status != null) {
                                predicates.add(cb.equal(root.get("status"), status));
                        }

                        if (leaveType != null && !leaveType.isEmpty()) {
                                predicates.add(cb.equal(root.get("leaveType"),
                                                leaveType.toUpperCase()));
                        }

                        if (startDate != null && !startDate.isEmpty()) {
                                LocalDate start = LocalDate.parse(startDate);
                                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), start));
                        }

                        if (endDate != null && !endDate.isEmpty()) {
                                LocalDate end = LocalDate.parse(endDate);
                                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), end));
                        }

                        if (search != null && !search.isEmpty()) {
                                String searchPattern = "%" + search.toLowerCase() + "%";
                                predicates.add(cb.or(
                                                cb.like(cb.lower(root.get("leaveType").as(String.class)),
                                                                searchPattern),
                                                cb.like(cb.lower(root.get("reason")), searchPattern),
                                                cb.like(cb.lower(root.get("status").as(String.class)),
                                                                searchPattern)));
                        }

                        return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };

                // Get paginated results
                Page<LeaveApplication> applications = leaveApplicationRepository.findAll(spec, pageable);

                return new ApiResponse<>("Leave applications retrieved successfully", applications, true,
                                HttpStatus.OK,
                                "leave_applications");
        }

        @Override
        public ApiResponse<LeaveApplication> removeDocument(UUID id, String filename) {
                LeaveApplication application = leaveApplicationRepository.findById(id)
                                .orElseThrow(() -> new AppException("Leave application not found",
                                                HttpStatus.NOT_FOUND));

                List<DocumentDto> currentDocuments = application.getSupportingDocuments();
                if (currentDocuments != null) {
                        List<DocumentDto> updatedDocuments = currentDocuments.stream()
                                        .filter(doc -> !doc.getFilePath().equals(filename))
                                        .collect(Collectors.toList());
                        application.setSupportingDocuments(updatedDocuments);

                        // Delete the file from storage
                        fileStorageService.delete(filename);

                        LeaveApplication updatedApplication = leaveApplicationRepository.save(application);
                        return new ApiResponse<>("Document removed successfully", updatedApplication, true,
                                        HttpStatus.OK,
                                        "leave_application");
                }

                throw new AppException("No documents found to remove", HttpStatus.NOT_FOUND);
        }

        @Override
        public ApiResponse<LeaveApplication> addDocument(UUID id, String filename) {
                LeaveApplication application = leaveApplicationRepository.findById(id)
                                .orElseThrow(() -> new AppException("Leave application not found",
                                                HttpStatus.NOT_FOUND));

                List<DocumentDto> currentDocuments = application.getSupportingDocuments();
                if (currentDocuments == null) {
                        currentDocuments = new ArrayList<>();
                }

                String type = filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".png")
                                ? "image"
                                : "file";
                DocumentDto newDocument = new DocumentDto(type, filename);
                currentDocuments.add(newDocument);

                application.setSupportingDocuments(currentDocuments);
                LeaveApplication updatedApplication = leaveApplicationRepository.save(application);

                return new ApiResponse<>("Document added successfully", updatedApplication, true, HttpStatus.OK,
                                "leave_application");
        }

        @Override
        public ApiResponse<Page<LeaveApplication>> getLeaveApplicationsByEmployeeWithFilters(
                        UUID employeeId,
                        LeaveStatus status,
                        String leaveType,
                        String startDate,
                        String endDate,
                        String search,
                        int page,
                        int size,
                        String sortDirection) {
                try {
                        Employee employee = employeeRepository.findById(employeeId)
                                        .orElseThrow(() -> new AppException("Employee not found",
                                                        HttpStatus.NOT_FOUND));

                        // Create Pageable with sorting
                        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
                        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

                        Specification<LeaveApplication> spec = (root, query, cb) -> {
                                List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
                                predicates.add(cb.equal(root.get("employee"), employee));

                                if (status != null) {
                                        predicates.add(cb.equal(root.get("status"), status));
                                }

                                if (leaveType != null && !leaveType.isEmpty()) {
                                        predicates.add(cb.equal(root.get("leaveType"), LeaveType.valueOf(leaveType)));
                                }

                                if (startDate != null && !startDate.isEmpty()) {
                                        LocalDate start = LocalDate.parse(startDate);
                                        predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), start));
                                }

                                if (endDate != null && !endDate.isEmpty()) {
                                        LocalDate end = LocalDate.parse(endDate);
                                        predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), end));
                                }

                                if (search != null && !search.isEmpty()) {
                                        String searchPattern = "%" + search.toLowerCase() + "%";
                                        predicates.add(cb.or(
                                                        cb.like(cb.lower(root.get("leaveType").as(String.class)),
                                                                        searchPattern),
                                                        cb.like(cb.lower(root.get("reason")), searchPattern),
                                                        cb.like(cb.lower(root.get("status").as(String.class)),
                                                                        searchPattern)));
                                }

                                return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                        };

                        // Get paginated results
                        Page<LeaveApplication> applications = leaveApplicationRepository.findAll(spec, pageable);

                        return new ApiResponse<>(
                                        "Leave applications retrieved successfully",
                                        applications,
                                        true,
                                        HttpStatus.OK,
                                        "leave_applications");
                } catch (Exception e) {
                        throw new AppException("Error retrieving leave applications: " + e.getMessage(),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @Override
        public ApiResponse<List<LeaveApplication>> getLeaveApplicationsByDate(String date, String department,
                        UUID employeeId) {
                LocalDate localDate = LocalDate.parse(date);
                List<LeaveApplication> applications = leaveApplicationRepository.findByStatus(LeaveStatus.APPROVED);
                if (department.equals("department")) {
                        System.out.println("department: " + department);

                        Employee employee = employeeRepository.findById(employeeId)
                                        .orElseThrow(() -> new AppException("Employee not found",
                                                        HttpStatus.NOT_FOUND));
                        String departmentId = employee.getDepartment().getId().toString();
                        System.out.println("departmentId: " + departmentId);
                        applications = applications.stream()
                                        .filter(application -> application.getEmployee().getDepartment()
                                                        .getId().toString().equals(departmentId))
                                        .collect(Collectors.toList());
                } else {
                        if (department != null && !department.isEmpty() && department.length() > 4) {
                                System.out.println("department: " + department);
                                applications = applications.stream()
                                                .filter(application -> application.getEmployee().getDepartment()
                                                                .getId().toString().equals(department))
                                                .collect(Collectors.toList());
                        }
                }
                applications = applications.stream()
                                .filter(application -> (application.getStartDate().isBefore(localDate)
                                                || application.getStartDate().isEqual(localDate))
                                                && (application.getEndDate().isAfter(localDate)
                                                                || application.getEndDate().isEqual(localDate)))
                                .collect(Collectors.toList());
                return new ApiResponse<>("Leave applications retrieved successfully", applications, true, HttpStatus.OK,
                                "leave_applications");
        }

        @Override
        public ResponseEntity<ByteArrayResource> exportLeaveApplications(LeaveStatus status, String leaveType,
                        String startDate,
                        String endDate, String search, int page, int size, String sortDirection) {
                List<LeaveApplication> applications = getLeaveApplicationsByStatus(status,
                                startDate, endDate, search,
                                leaveType, 0, 1000, sortDirection).getData().getContent();
                if (applications.isEmpty()) {
                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=leave_applications.csv")
                                        .body(new ByteArrayResource(new byte[0]));
                }
                String[] headers = { "Employee Name", "Leave Type", "Start Date", "End Date", "Status", "Reason" };
                String[][] data = new String[applications.size()][headers.length];

                for (int i = 0; i < applications.size(); i++) {
                        LeaveApplication application = applications.get(i);
                        data[i] = new String[] { application.getEmployee().getName(),
                                        application.getLeaveType().toString(),
                                        application.getStartDate().toString(),
                                        application.getEndDate().toString(),
                                        application.getStatus().toString(),
                                        application.getReason() };
                }

                String csvContent = convertToCsv(headers, data);
                ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave_applications.csv")
                                .body(resource);
        }

        private String convertToCsv(String[] headers, String[][] data) {
                StringBuilder csvBuilder = new StringBuilder();
                csvBuilder.append(String.join(",", headers));
                csvBuilder.append("\n");
                for (String[] row : data) {
                        csvBuilder.append(String.join(",", row));
                        csvBuilder.append("\n");
                }
                return csvBuilder.toString();
        }
}
