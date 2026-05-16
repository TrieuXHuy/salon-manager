package com.salonnbooking.api.dto;

public final class AdminStaffServiceDtos {

    private AdminStaffServiceDtos() {
    }

    public record StaffServiceResponse(
            Long id,
            Long staffId,
            String staffName,
            Long serviceId,
            String serviceName,
            Boolean staffActive,
            Boolean serviceActive) {
    }
}
