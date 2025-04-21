package com.example.leave_management.controller;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Employee.UserRole;
import com.example.leave_management.model.Holiday;
import com.example.leave_management.security.RequiresLogin;
import com.example.leave_management.service.HolidayService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.leave_management.security.RequiresRole;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/holidays")
@SecurityRequirement(name = "bearerAuth")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @PostMapping
    public ResponseEntity<ApiResponse<Holiday>> createHoliday(@RequestBody Holiday holiday) {
        return ResponseEntity.ok(holidayService.createHoliday(holiday));
    }

    @PutMapping("/{holidayId}")
    public ResponseEntity<ApiResponse<Holiday>> updateHoliday(
            @PathVariable UUID holidayId,
            @RequestBody Holiday holiday) {
        return ResponseEntity.ok(holidayService.updateHoliday(holidayId, holiday));
    }

    @RequiresLogin
    @GetMapping("/{holidayId}")
    public ResponseEntity<ApiResponse<Holiday>> getHolidayById(@PathVariable UUID holidayId) {
        return ResponseEntity.ok(holidayService.getHolidayById(holidayId));
    }

    @RequiresLogin
    @GetMapping
    public ResponseEntity<ApiResponse<List<Holiday>>> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<Holiday>>> getHolidaysByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(holidayService.getHolidaysByYear(year));
    }

    @GetMapping("/between-dates")
    public ResponseEntity<ApiResponse<List<Holiday>>> getHolidaysBetweenDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(holidayService.getHolidaysBetweenDates(startDate, endDate));
    }

    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @DeleteMapping("/{holidayId}")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable UUID holidayId) {
        return ResponseEntity.ok(holidayService.deleteHoliday(holidayId));
    }

    @RequiresLogin
    @RequiresRole({ UserRole.ADMIN, UserRole.MANAGER })
    @PutMapping("/{holidayId}/toggle-status")
    public ResponseEntity<ApiResponse<Holiday>> toggleHolidayStatus(@PathVariable UUID holidayId) {
        return ResponseEntity.ok(holidayService.toggleHolidayStatus(holidayId));
    }

    @PutMapping("/{holidayId}/restrict")
    public ResponseEntity<ApiResponse<Holiday>> restrictHoliday(
            @PathVariable UUID holidayId,
            @RequestParam(required = false) String reason) {
        Holiday holiday = new Holiday();
        holiday.setId(holidayId);
        holiday.setRestricted(true);
        holiday.setRestrictionReason(reason);
        return ResponseEntity.ok(holidayService.updateHoliday(holidayId, holiday));
    }

    @PutMapping("/{holidayId}/unrestrict")
    public ResponseEntity<ApiResponse<Holiday>> unrestrictHoliday(@PathVariable UUID holidayId) {
        Holiday holiday = new Holiday();
        holiday.setId(holidayId);
        holiday.setRestricted(false);
        holiday.setRestrictionReason(null);
        return ResponseEntity.ok(holidayService.updateHoliday(holidayId, holiday));
    }
}