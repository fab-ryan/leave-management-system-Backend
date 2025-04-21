package com.example.leave_management.service;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.model.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HolidayService {
    ApiResponse<Holiday> createHoliday(Holiday holiday);

    ApiResponse<Holiday> updateHoliday(UUID holidayId, Holiday holiday);

    ApiResponse<Holiday> getHolidayById(UUID holidayId);

    ApiResponse<List<Holiday>> getAllHolidays();

    ApiResponse<List<Holiday>> getHolidaysByYear(Integer year);

    ApiResponse<List<Holiday>> getHolidaysBetweenDates(LocalDate startDate, LocalDate endDate);

    ApiResponse<Void> deleteHoliday(UUID holidayId);

    ApiResponse<Holiday> toggleHolidayStatus(UUID holidayId);
}