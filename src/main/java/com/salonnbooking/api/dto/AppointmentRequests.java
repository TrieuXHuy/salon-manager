package com.salonnbooking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.salonnbooking.domain.AppointmentStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public final class AppointmentRequests {
	private AppointmentRequests() {
	}

	public record ServiceLine(
			@NotNull Integer serviceId) {
	}

	public record Create(
			@NotNull Integer customerId,
			@NotNull Integer employeeId,
			@NotEmpty List<@NotNull Integer> serviceIds,
			@NotNull LocalDateTime appointmentTime,
			AppointmentStatus status,
			String note) {
	}

	public record Update(
			@NotNull Integer customerId,
			@NotNull Integer employeeId,
			@NotEmpty List<@NotNull Integer> serviceIds,
			@NotNull LocalDateTime appointmentTime,
			@NotNull AppointmentStatus status,
			String note) {
	}

	public record AppointmentServiceResponse(
			Integer id,
			Integer serviceId,
			String serviceName,
			BigDecimal price,
			Integer durationMinutes,
			Integer assignedEmployeeId) {
		public static AppointmentServiceResponse from(com.salonnbooking.domain.AppointmentServiceItem item) {
			return new AppointmentServiceResponse(
					item.getId(),
					item.getService().getId(),
					item.getServiceNameSnapshot(),
					item.getPrice(),
					item.getDurationMinutes(),
					item.getAssignedEmployee() != null ? item.getAssignedEmployee().getId() : null);
		}
	}

	public record Response(
			Integer id,
			Integer customerId,
			Integer employeeId,
			String employeeName,
			LocalDateTime appointmentTime,
			LocalDateTime estimatedEndTime,
			AppointmentStatus status,
			String note,
			List<AppointmentServiceResponse> services,
			BigDecimal subtotal,
			Integer totalDurationMinutes,
			String primaryServiceName,
			String serviceSummary) {
		public static Response from(com.salonnbooking.domain.Appointment appointment) {
			List<AppointmentServiceResponse> services = appointment.getAppointmentServices().stream()
					.map(AppointmentServiceResponse::from)
					.toList();
			BigDecimal subtotal = appointment.getAppointmentServices().stream()
					.map(com.salonnbooking.domain.AppointmentServiceItem::getPrice)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			int totalDuration = appointment.getAppointmentServices().stream()
					.mapToInt(com.salonnbooking.domain.AppointmentServiceItem::getDurationMinutes)
					.sum();
			String primaryServiceName = services.isEmpty() ? null : services.get(0).serviceName();
			String serviceSummary = services.stream()
					.map(AppointmentServiceResponse::serviceName)
					.reduce((left, right) -> left + ", " + right)
					.orElse("");

			Integer employeeId = appointment.getEmployee() != null ? appointment.getEmployee().getId() : null;
			String employeeName = appointment.getEmployee() != null ? appointment.getEmployee().getFullName() : "Chua phan cong";
			LocalDateTime estimatedEndTime = appointment.getEstimatedEndTime() != null
					? appointment.getEstimatedEndTime()
					: appointment.getAppointmentTime().plusMinutes(totalDuration);

			return new Response(
					appointment.getId(),
					appointment.getCustomer().getId(),
					employeeId,
					employeeName,
					appointment.getAppointmentTime(),
					estimatedEndTime,
					appointment.getStatus(),
					appointment.getNote(),
					services,
					subtotal,
					totalDuration,
					primaryServiceName,
					serviceSummary);
		}
	}
}
