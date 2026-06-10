package com.salonnbooking.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStage;
import com.salonnbooking.domain.PaymentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class PaymentRequests {
	private PaymentRequests() {
	}

	public record Create(
			@NotNull Integer appointmentId,
			@NotNull @Positive BigDecimal amount,
			PaymentMethod paymentMethod,
			PaymentStatus paymentStatus,
			LocalDateTime paidAt,
			PaymentStage paymentStage) {
		public Create(Integer appointmentId, BigDecimal amount, PaymentMethod paymentMethod,
				PaymentStatus paymentStatus, LocalDateTime paidAt) {
			this(appointmentId, amount, paymentMethod, paymentStatus, paidAt, null);
		}
	}

	public record Update(
			@NotNull Integer appointmentId,
			@NotNull @Positive BigDecimal amount,
			PaymentMethod paymentMethod,
			@NotNull PaymentStatus paymentStatus,
			LocalDateTime paidAt,
			PaymentStage paymentStage) {
		public Update(Integer appointmentId, BigDecimal amount, PaymentMethod paymentMethod,
				PaymentStatus paymentStatus, LocalDateTime paidAt) {
			this(appointmentId, amount, paymentMethod, paymentStatus, paidAt, null);
		}
	}

	public record Response(
			Integer id,
			Integer appointmentId,
			BigDecimal amount,
			PaymentMethod paymentMethod,
			PaymentStage paymentStage,
			PaymentStatus paymentStatus,
			LocalDateTime paidAt) {
		public static Response from(com.salonnbooking.domain.Payment p) {
			return new Response(
					p.getId(),
					p.getAppointment().getId(),
					p.getAmount(),
					p.getPaymentMethod(),
					p.getPaymentStage(),
					p.getPaymentStatus(),
					p.getPaidAt());
		}
	}
}
