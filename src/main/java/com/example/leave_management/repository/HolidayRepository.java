package com.example.leave_management.repository;

import com.example.leave_management.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Holiday> findByIsRecurringAndIsActive(Boolean isRecurring, Boolean isActive);

    List<Holiday> findByDateAndIsActive(LocalDate date, Boolean isActive);
}