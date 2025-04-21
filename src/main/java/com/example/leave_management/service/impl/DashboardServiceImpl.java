package com.example.leave_management.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.dto.response.ManagerDashboardStats;
import com.example.leave_management.enums.LeaveType;
import com.example.leave_management.model.Department;
import com.example.leave_management.model.Employee;
import com.example.leave_management.model.LeaveApplication;
import com.example.leave_management.model.LeaveApplication.LeaveStatus;
import com.example.leave_management.model.LeaveBalance;
import com.example.leave_management.repository.DepartmentRepository;
import com.example.leave_management.repository.EmployeeRepository;
import com.example.leave_management.repository.LeaveApplicationRepository;
import com.example.leave_management.repository.LeaveBalanceRepository;
import com.example.leave_management.service.DashboardService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;

import jakarta.transaction.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardServiceImpl implements DashboardService {

        @Autowired
        private LeaveApplicationRepository leaveRequestRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private DepartmentRepository departmentRepository;

        @Autowired
        private LeaveBalanceRepository leaveBalanceRepository;

        public ApiResponse<Object> getEmployeeDashboard(UUID employeeId) {
                try {
                        Employee employee = employeeRepository.findById(employeeId)
                                        .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

                        // Get upcoming leaves
                        List<LeaveApplication> upcomingLeaves = leaveRequestRepository
                                        .findByEmployeeIdAndStartDateAfter(employeeId,
                                                        LocalDate.now());

                        // Get approved leaves count
                        long approvedLeavesCount = leaveRequestRepository
                                        .countByEmployeeAndStatus(employee, LeaveStatus.APPROVED);

                        // Get pending leaves count
                        long pendingLeavesCount = leaveRequestRepository
                                        .countByEmployeeAndStatus(employee, LeaveStatus.PENDING);

                        Map<String, Object> dashboardData = new HashMap<>();
                        dashboardData.put("upcomingLeaves", upcomingLeaves);
                        dashboardData.put("approvedLeavesCount", approvedLeavesCount);
                        dashboardData.put("pendingLeavesCount", pendingLeavesCount);

                        return new ApiResponse<>("Dashboard data retrieved successfully", dashboardData, true,
                                        HttpStatus.OK, "dashboard");
                } catch (Exception e) {
                        return new ApiResponse<>("Error retrieving dashboard data", null, false,
                                        HttpStatus.INTERNAL_SERVER_ERROR, "error");
                }
        }

        @Override
        public ApiResponse<ManagerDashboardStats> getManagerDashboardStats(String departmentId) {
                int year = LocalDate.now().getYear();
                ManagerDashboardStats stats = new ManagerDashboardStats();

                Map<String, Map<String, Long>> leaveTypeMonthlyStats = new HashMap<>();

                // Initialize all months
                String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
                                "Dec" };
                for (String monthName : monthNames) {
                        leaveTypeMonthlyStats.put(monthName, new HashMap<>());
                }

                // Get all approved leave applications for the year
                List<LeaveApplication> approvedLeaves = leaveRequestRepository
                                .findByStatusAndStartDateBetween(
                                                LeaveStatus.APPROVED,
                                                LocalDate.of(year, 1, 1),
                                                LocalDate.of(year, 12, 31));

                // Sort leaves by month
                approvedLeaves.sort(Comparator.comparing(leave -> leave.getStartDate().getMonthValue()));

                // Group leaves by month and type
                for (LeaveApplication leave : approvedLeaves) {
                        String monthName = leave.getStartDate().getMonth().getDisplayName(TextStyle.SHORT,
                                        Locale.ENGLISH);
                        String leaveType = leave.getLeaveType().toString();

                        Map<String, Long> monthStats = leaveTypeMonthlyStats.get(monthName);
                        monthStats.merge(leaveType, 1L, Long::sum);

                        // Initialize all leave types with 0 for this month if not present
                        for (LeaveType type : LeaveType.values()) {
                                monthStats.putIfAbsent(type.toString(), 0L);
                        }
                }

                // Initialize all leave types for each month with 0 if not present
                Set<String> allLeaveTypes = approvedLeaves.stream()
                                .map(leave -> leave.getLeaveType().toString())
                                .collect(Collectors.toSet());

                // Sort months in chronological order
                List<String> sortedMonths = Arrays.asList(monthNames);
                for (String month : sortedMonths) {
                        Map<String, Long> monthStats = leaveTypeMonthlyStats.get(month);
                        for (String leaveType : allLeaveTypes) {
                                monthStats.putIfAbsent(leaveType, 0L);
                        }
                }

                stats.setLeaveTypeMonthlyStats(leaveTypeMonthlyStats);

                // Get department leave counts
                Map<String, Map<String, Long>> departmentLeaveCounts = new HashMap<>();

                if (departmentId.startsWith("all")) {
                        List<Department> departments = departmentRepository.findAll();

                        for (Department department : departments) {
                                Map<String, Long> leaveCounts = new HashMap<>();

                                // Get all leave types for this department
                                List<LeaveApplication> departmentLeaves = leaveRequestRepository
                                                .findByEmployeeDepartmentAndStatus(department, LeaveStatus.APPROVED);

                                // Count leaves by type
                                Map<String, Long> typeCounts = departmentLeaves.stream()
                                                .collect(Collectors.groupingBy(
                                                                leave -> leave.getLeaveType().toString(),
                                                                Collectors.counting()));

                                // Initialize all leave types with 0
                                for (String leaveType : allLeaveTypes) {
                                        leaveCounts.put(leaveType, typeCounts.getOrDefault(leaveType, 0L));
                                }

                                departmentLeaveCounts.put(department.getName(), leaveCounts);
                        }
                } else {
                        Department department = departmentRepository.findById(UUID.fromString(departmentId))
                                        .orElseThrow(() -> new IllegalArgumentException("Department not found"));

                        Map<String, Long> leaveCounts = new HashMap<>();
                        for (LeaveType type : LeaveType.values()) {
                                leaveCounts.put(type.toString(), 0L);
                        }
                        departmentLeaveCounts.put(department.getName(), leaveCounts);
                }

                stats.setDepartmentLeaveDays(departmentLeaveCounts);

                // Get status counts and ratios
                Map<LeaveStatus, Long> statusCounts = new HashMap<>();
                Map<LeaveStatus, Double> statusRatios = new HashMap<>();
                long totalApplications = leaveRequestRepository.count();

                for (LeaveStatus status : LeaveStatus.values()) {
                        long count = leaveRequestRepository.countByStatus(status);
                        statusCounts.put(status, count);
                        statusRatios.put(status, totalApplications > 0 ? (double) count / totalApplications : 0.0);
                }

                stats.setStatusCounts(statusCounts);
                stats.setStatusRatios(statusRatios);

                return new ApiResponse<>("Manager dashboard statistics retrieved successfully", stats, true,
                                HttpStatus.OK, "dashboard");
        }

        @Override
        public ResponseEntity<byte[]> exportDashboardData(String format, LocalDate startDate, LocalDate endDate) {
                try {
                        byte[] data;
                        String contentType;

                        switch (format.toLowerCase()) {
                                case "csv":
                                        data = exportToCSV(startDate, endDate);
                                        contentType = "text/csv";
                                        break;
                                case "excel":
                                        data = exportToExcel(startDate, endDate);
                                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                                        break;
                                case "pdf":
                                        data = exportToPDF(startDate, endDate);
                                        contentType = "application/pdf";
                                        break;
                                default:
                                        throw new IllegalArgumentException("Unsupported format: " + format);
                        }

                        return ResponseEntity.ok()
                                        .header("Content-Type", contentType)
                                        .header("Content-Disposition", "attachment; filename=leave_report." + format)
                                        .body(data);

                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
        }

        private byte[] exportToCSV(LocalDate startDate, LocalDate endDate) throws IOException {
                StringWriter writer = new StringWriter();
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                                .withHeader("Employee", "Department", "Leave Type", "Start Date", "End Date", "Status",
                                                "Days"));

                List<LeaveApplication> applications = leaveRequestRepository
                                .findByStartDateBetween(startDate, endDate);

                for (LeaveApplication app : applications) {
                        csvPrinter.printRecord(
                                        app.getEmployee().getName(),
                                        app.getEmployee().getDepartment().getName(),
                                        app.getLeaveType().toString(),
                                        app.getStartDate(),
                                        app.getEndDate(),
                                        app.getStatus(),
                                        app.getEndDate().toEpochDay() - app.getStartDate().toEpochDay() + 1);
                }

                csvPrinter.flush();
                return writer.toString().getBytes();
        }

        private byte[] exportToExcel(LocalDate startDate, LocalDate endDate) throws IOException {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Leave Report");

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = { "Employee", "Department", "Leave Type", "Start Date", "End Date", "Status",
                                "Days" };
                for (int i = 0; i < headers.length; i++) {
                        headerRow.createCell(i).setCellValue(headers[i]);
                }

                // Add data rows
                List<LeaveApplication> applications = leaveRequestRepository
                                .findByStartDateBetween(startDate, endDate);

                int rowNum = 1;
                for (LeaveApplication app : applications) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(app.getEmployee().getName());
                        row.createCell(1).setCellValue(app.getEmployee().getDepartment().getName());
                        row.createCell(2).setCellValue(app.getLeaveType().toString());
                        row.createCell(3).setCellValue(app.getStartDate().toString());
                        row.createCell(4).setCellValue(app.getEndDate().toString());
                        row.createCell(5).setCellValue(app.getStatus().toString());
                        row.createCell(6).setCellValue(
                                        app.getEndDate().toEpochDay() - app.getStartDate().toEpochDay() + 1);
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                        sheet.autoSizeColumn(i);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                workbook.close();

                return outputStream.toByteArray();
        }

        private byte[] exportToPDF(LocalDate startDate, LocalDate endDate) {
                // TODO: Implement PDF export using a library like iText or Apache PDFBox
                return new byte[0];
        }
}
