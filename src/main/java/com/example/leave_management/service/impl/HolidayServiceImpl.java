package com.example.leave_management.service.impl;

import com.example.leave_management.dto.response.ApiResponse;
import com.example.leave_management.exception.AppException;
import com.example.leave_management.model.Holiday;
import com.example.leave_management.repository.HolidayRepository;
import com.example.leave_management.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Override
    public ApiResponse<Holiday> createHoliday(Holiday holiday) {
        if (holiday.getDate() == null) {
            throw new AppException("Holiday date is required", HttpStatus.BAD_REQUEST);
        }

        // Check for existing holiday on the same date
        List<Holiday> existingHolidays = holidayRepository.findByDateAndIsActive(holiday.getDate(), true);
        if (!existingHolidays.isEmpty()) {
            throw new AppException("A holiday already exists on this date", HttpStatus.CONFLICT);
        }

        // Set default values for new fields
        holiday.setActive(true);
        holiday.setRestricted(false);
        if (holiday.isRestricted()
                && (holiday.getRestrictionReason() == null || holiday.getRestrictionReason().trim().isEmpty())) {
            throw new AppException("Restriction reason is required when holiday is restricted", HttpStatus.BAD_REQUEST);
        }

        Holiday savedHoliday = holidayRepository.save(holiday);
        return new ApiResponse<>("Holiday created successfully", savedHoliday, true, HttpStatus.CREATED, "holiday");
    }

    @Override
    public ApiResponse<Holiday> updateHoliday(UUID holidayId, Holiday holiday) {
        Holiday existingHoliday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new AppException("Holiday not found", HttpStatus.NOT_FOUND));

        // Update fields if provided
        if (holiday.getName() != null) {
            existingHoliday.setName(holiday.getName());
        }
        if (holiday.getDate() != null) {
            existingHoliday.setDate(holiday.getDate());
        }
        existingHoliday.setRecurring(holiday.isRecurring());
        existingHoliday.setRestricted(holiday.isRestricted());
        if (holiday.isRestricted()
                && (holiday.getRestrictionReason() == null || holiday.getRestrictionReason().trim().isEmpty())) {
            throw new AppException("Restriction reason is required when holiday is restricted", HttpStatus.BAD_REQUEST);
        }
        if (holiday.getRestrictionReason() != null) {
            existingHoliday.setRestrictionReason(holiday.getRestrictionReason());
        }

        Holiday updatedHoliday = holidayRepository.save(existingHoliday);
        return new ApiResponse<>("Holiday updated successfully", updatedHoliday, true, HttpStatus.OK, "holiday");
    }

    @Override
    public ApiResponse<Holiday> getHolidayById(UUID holidayId) {
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new AppException("Holiday not found", HttpStatus.NOT_FOUND));
        return new ApiResponse<>("Holiday retrieved successfully", holiday, true, HttpStatus.OK, "holiday");
    }

    @Override
    public ApiResponse<List<Holiday>> getAllHolidays() {
        List<Holiday> holidays = holidayRepository.findAll();
        return new ApiResponse<>("All holidays retrieved successfully", holidays, true, HttpStatus.OK, "holidays");
    }

    @Override
    public ApiResponse<List<Holiday>> getHolidaysByYear(Integer year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        List<Holiday> holidays = holidayRepository.findByDateBetween(startDate, endDate);
        return new ApiResponse<>("Holidays for year " + year + " retrieved successfully", holidays, true, HttpStatus.OK,
                "holidays");
    }

    @Override
    public ApiResponse<List<Holiday>> getHolidaysBetweenDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new AppException("Start date must be before end date", HttpStatus.BAD_REQUEST);
        }
        List<Holiday> holidays = holidayRepository.findByDateBetween(startDate, endDate);
        return new ApiResponse<>("Holidays between dates retrieved successfully", holidays, true, HttpStatus.OK,
                "holidays");
    }

    @Override
    public ApiResponse<Void> deleteHoliday(UUID holidayId) {
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new AppException("Holiday not found", HttpStatus.NOT_FOUND));

        holidayRepository.delete(holiday);
        return new ApiResponse<>("Holiday deleted successfully", null, true, HttpStatus.OK, "holiday");
    }

    @Override
    public ApiResponse<Holiday> toggleHolidayStatus(UUID holidayId) {
        Holiday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new AppException("Holiday not found", HttpStatus.NOT_FOUND));

        holiday.setActive(!holiday.isActive());
        Holiday updatedHoliday = holidayRepository.save(holiday);

        String message = holiday.isActive() ? "Holiday activated successfully" : "Holiday deactivated successfully";
        return new ApiResponse<>(message, updatedHoliday, true, HttpStatus.OK, "holiday");
    }
}