package com.example.leave_management.dto;

import java.time.LocalDate;

public class CompassiionRequestDto {
    private LocalDate workDate;

    private String reason;

    private boolean isHoliday;

    private boolean isWeekend;

    public void setHoliday(boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public void setWeekend(boolean isWeekend) {
        this.isWeekend = isWeekend;
    }

    public String getReason() {
        return reason;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public boolean isWeekend() {
        return isWeekend;
    }
}
