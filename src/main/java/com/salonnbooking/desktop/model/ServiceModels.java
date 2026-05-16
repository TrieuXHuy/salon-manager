package com.salonnbooking.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ServiceModels {

    private ServiceModels() {
    }

    public record ServiceResponse(
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
