package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.salonnbooking.domain.Appointment;

@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final Resend resend;
	private final String fromEmail;

	public EmailService(@Value("${resend.api-key}") String apiKey, @Value("${resend.from-email}") String fromEmail) {
		this.resend = new Resend(apiKey);
		this.fromEmail = fromEmail;
	}

	public void sendBookingConfirmation(Appointment appointment) {
		if (appointment == null || appointment.getCustomer() == null) {
			return;
		}

		String toEmail = appointment.getCustomer().getEmail();
		if (toEmail == null || toEmail.trim().isEmpty()) {
			log.info("Skipping booking confirmation, customer has no email. appointmentId={}", appointment.getId());
			return;
		}

		String htmlContent;
		try {
			htmlContent = buildConfirmationHtml(appointment);
		} catch (Exception e) {
			log.error("Failed to build booking confirmation email for appointmentId={}", appointment.getId(), e);
			return;
		}

		final Integer appointmentId = appointment.getId();
		final String finalHtml = htmlContent;

		CompletableFuture.runAsync(() -> sendEmail(toEmail, appointmentId, "Xác nhận đặt cọc thành công - Salon Booking", finalHtml));
	}

	public void sendAppointmentReminder(Appointment appointment) {
		if (appointment == null || appointment.getCustomer() == null) {
			return;
		}

		String toEmail = appointment.getCustomer().getEmail();
		if (toEmail == null || toEmail.trim().isEmpty()) {
			log.info("Skipping reminder, customer has no email. appointmentId={}", appointment.getId());
			return;
		}

		String htmlContent;
		try {
			htmlContent = buildReminderHtml(appointment);
		} catch (Exception e) {
			log.error("Failed to build reminder email for appointmentId={}", appointment.getId(), e);
			return;
		}

		final Integer appointmentId = appointment.getId();
		final String finalHtml = htmlContent;

		CompletableFuture.runAsync(() -> sendEmail(toEmail, appointmentId, "Nhắc lịch hẹn dịch vụ tại Salon Booking", finalHtml));
	}

	private void sendEmail(String toEmail, Integer appointmentId, String subject, String html) {
		try {
			CreateEmailOptions request = CreateEmailOptions.builder()
					.from(fromEmail)
					.to(toEmail)
					.subject(subject)
					.html(html)
					.build();
			CreateEmailResponse response = resend.emails().send(request);
			log.info("Email sent successfully. appointmentId={}, emailId={}", appointmentId, response.getId());
		} catch (Exception e) {
			log.error("Failed to send email. appointmentId={}", appointmentId, e);
		}
	}

	private String buildReminderHtml(Appointment appointment) {
		return buildHtml(
				"Nhắc lịch hẹn dịch vụ",
				"Bạn đã đặt cọc. Salon đã giữ slot cho bạn.",
				appointment);
	}

	private String buildConfirmationHtml(Appointment appointment) {
		return buildHtml(
				"Xác nhận đặt cọc",
				"Cảm ơn bạn đã đặt cọc. Salon đã giữ slot cho bạn.",
				appointment);
	}

	private String buildHtml(String title, String intro, Appointment appointment) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		String formattedTime = appointment.getAppointmentTime() == null ? "N/A" : appointment.getAppointmentTime().format(formatter);
		String customerName = safe(appointment.getCustomer().getFullName());
		String serviceName = appointment.getService() == null ? "N/A" : safe(appointment.getService().getName());
		String totalAmount = money(appointment.getTotalAmount(), appointment.getService() == null ? null : appointment.getService().getPrice());
		String depositAmount = money(appointment.getDepositAmount(), null);
		String remainingAmount = money(appointment.getRemainingAmount(), null);
		String roomName = appointment.getRoom() == null ? "N/A" : safe(appointment.getRoom().getName());
		String note = (appointment.getNote() == null || appointment.getNote().trim().isEmpty()) ? "Không có" : appointment.getNote();
		Integer appointmentId = appointment.getId();

		return """
				<!DOCTYPE html>
				<html>
				<head>
				  <meta charset='UTF-8'>
				  <title>%s</title>
				</head>
				<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 20px; color: #333;'>
				  <div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); border: 1px solid #e0e0e0;'>
				    <div style='background: linear-gradient(135deg, #6a11cb, #2575fc); padding: 30px; text-align: center; color: white;'>
				      <h1 style='margin: 0; font-size: 24px; font-weight: bold;'>Salon Booking System</h1>
				      <p style='margin: 5px 0 0; font-size: 14px; opacity: 0.9;'>%s</p>
				    </div>
				    <div style='padding: 30px; line-height: 1.6;'>
				      <h2 style='color: #2575fc; margin-top: 0;'>Xin chào %s,</h2>
				      <p>%s</p>
				      <div style='background-color: #f8f9fa; border-left: 4px solid #2575fc; padding: 20px; border-radius: 6px; margin: 25px 0;'>
				        <table style='width: 100%%; border-collapse: collapse;'>
				          %s
				          %s
				          %s
				          %s
				          %s
				          %s
				          %s
				          %s
				        </table>
				      </div>
				    </div>
				    <div style='background-color: #f4f4f9; padding: 20px; text-align: center; font-size: 12px; color: #777; border-top: 1px solid #e0e0e0;'>
				      <p style='margin: 0;'>Đây là email tự động từ hệ thống Salon Booking. Vui lòng không trả lời trực tiếp email này.</p>
				    </div>
				  </div>
				</body>
				</html>
				""".formatted(
				title,
				title,
				escape(customerName),
				intro,
				row("Mã lịch hẹn", "#" + safe(appointmentId)),
				row("Thời gian", formattedTime),
				row("Dịch vụ", serviceName),
				row("Tổng tiền", totalAmount),
				row("Tiền cọc", depositAmount),
				row("Còn lại", remainingAmount),
				row("Phòng dịch vụ", roomName),
				row("Ghi chú", note));
	}

	private String row(String label, String value) {
		return "<tr>"
				+ "<td style='padding: 6px 0; font-weight: bold; color: #555; width: 150px;'>" + escape(label) + ":</td>"
				+ "<td style='padding: 6px 0; color: #333;'>" + escape(value) + "</td>"
				+ "</tr>";
	}

	private String money(BigDecimal preferred, BigDecimal fallback) {
		BigDecimal value = preferred != null && preferred.compareTo(BigDecimal.ZERO) > 0 ? preferred : fallback;
		if (value == null) {
			return "N/A";
		}
		return String.format("%,.0f VNĐ", value);
	}

	private String safe(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
