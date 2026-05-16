package com.salonnbooking.api.dto;

public final class ServiceCategoryDtos {

    private ServiceCategoryDtos() {
    }

    public record UpsertRequest(
            String name,
            String description,
            Boolean isActive) {
    }

    public record Response(
            Long id,
            String name,
            String description,
            Boolean isActive) {
    }
}
