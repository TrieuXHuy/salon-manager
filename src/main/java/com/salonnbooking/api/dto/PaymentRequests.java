package com.salonnbooking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public final class PaymentRequests {
	private PaymentRequests() {
	}

	public record Create(
			@NotNull Integer appointmentId,
			@PositiveOrZero BigDecimal subtotal,
			@PositiveOrZero BigDecimal discountAmount,
			@PositiveOrZero BigDecimal finalAmount,
			PaymentMethod paymentMethod,
			PaymentStatus paymentStatus,
			LocalDateTime paidAt) {
	}

	public record Update(
			@NotNull Integer appointmentId,
			@PositiveOrZero BigDecimal subtotal,
			@PositiveOrZero BigDecimal discountAmount,
			@PositiveOrZero BigDecimal finalAmount,
			PaymentMethod paymentMethod,
			@NotNull PaymentStatus paymentStatus,
			LocalDateTime paidAt) {
	}

	public record Response(
			Integer id,
			Integer appointmentId,
			BigDecimal subtotal,
			BigDecimal discountAmount,
			BigDecimal finalAmount,
			PaymentMethod paymentMethod,
			PaymentStatus paymentStatus,
			LocalDateTime paidAt) {
		public static Response from(com.salonnbooking.domain.Payment p) {
			return new Response(
					p.getId(),
					p.getAppointment().getId(),
					p.getSubtotal(),
					p.getDiscountAmount(),
					p.getFinalAmount(),
					p.getPaymentMethod(),
					p.getPaymentStatus(),
					p.getPaidAt());
		}
	}
}
