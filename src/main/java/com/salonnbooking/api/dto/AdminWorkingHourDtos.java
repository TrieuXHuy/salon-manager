package com.salonnbooking.api.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public final class AdminWorkingHourDtos {

    private AdminWorkingHourDtos() {
    }

    public record CreateWorkingHourRequest(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Boolean isActive) {
    }

    public record UpdateWorkingHourRequest(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Boolean isActive) {
    }

    public record WorkingHourResponse(
            Long id,
            Long staffId,
            String staffName,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Boolean isActive) {
    }
}
