package com.salonnbooking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ServiceDtos {

    private ServiceDtos() {
    }

    public record UpsertRequest(
            Long categoryId,
            String name,
            String description,
            BigDecimal price,
            Integer durationMinutes,
            Boolean isActive) {
    }

    public record Response(
            Long id,
            Long categoryId,
            String categoryName,
            String name,
            String description,
            BigDecimal price,
            Integer durationMinutes,
            Boolean isActive,
            LocalDateTime createdAt) {
    }
}
