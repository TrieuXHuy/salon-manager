package com.salonnbooking.api.dto;

import java.time.LocalDateTime;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            Long appointmentId,
            Integer rating,
            String comment) {
    }

    public record ReviewResponse(
            Long id,
            Long appointmentId,
            Long customerId,
            String customerName,
            Long staffId,
            String staffName,
            Integer rating,
            String comment,
            LocalDateTime createdAt) {
    }
}
