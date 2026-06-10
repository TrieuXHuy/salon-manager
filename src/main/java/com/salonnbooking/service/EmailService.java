package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.salonnbooking.domain.Appointment;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final String fromEmail;

	public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username:}") String fromEmail) {
		this.mailSender = mailSender;
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

		try {
			String htmlContent = buildConfirmationHtml(appointment);
			sendEmail(toEmail, appointment.getId(), "Xac nhan dat coc thanh cong - Salon Pro", htmlContent);
		} catch (Exception e) {
			log.error("Failed to send booking confirmation. appointmentId={}", appointment.getId(), e);
		}
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

		String htmlContent = buildReminderHtml(appointment);
		sendEmail(toEmail, appointment.getId(), "Nhac lich hen dich vu tai Salon Pro", htmlContent);
	}

	private void sendEmail(String toEmail, Integer appointmentId, String subject, String html) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			if (fromEmail != null && !fromEmail.isBlank()) {
				helper.setFrom(fromEmail);
			}
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(html, true);

			mailSender.send(message);
			log.info("Email sent successfully. appointmentId={}, to={}", appointmentId, toEmail);
		} catch (Exception e) {
			log.error("Failed to send email. appointmentId={}", appointmentId, e);
			throw new IllegalStateException("Failed to send email", e);
		}
	}

	private String buildReminderHtml(Appointment appointment) {
		return buildHtml(
				"Nhac lich hen dich vu",
				"Ban da dat coc. Salon da giu cho cho ban.",
				appointment);
	}

	private String buildConfirmationHtml(Appointment appointment) {
		return buildHtml(
				"Xac nhan dat coc",
				"Cam on ban da dat coc. Salon da giu cho cho ban.",
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
		String note = (appointment.getNote() == null || appointment.getNote().trim().isEmpty()) ? "Khong co" : appointment.getNote();
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
				      <h1 style='margin: 0; font-size: 24px; font-weight: bold;'>Salon Pro</h1>
				      <p style='margin: 5px 0 0; font-size: 14px; opacity: 0.9;'>%s</p>
				    </div>
				    <div style='padding: 30px; line-height: 1.6;'>
				      <h2 style='color: #2575fc; margin-top: 0;'>Xin chao %s,</h2>
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
				      <p style='margin: 0;'>Day la email tu dong tu he thong Salon Pro. Vui long khong tra loi truc tiep email nay.</p>
				    </div>
				  </div>
				</body>
				</html>
				""".formatted(
				title,
				title,
				escape(customerName),
				escape(intro),
				row("Ma lich hen", "#" + safe(appointmentId)),
				row("Thoi gian", formattedTime),
				row("Dich vu", serviceName),
				row("Tong tien", totalAmount),
				row("Tien coc", depositAmount),
				row("Con lai", remainingAmount),
				row("Phong dich vu", roomName),
				row("Ghi chu", note));
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
		return String.format("%,.0f VND", value);
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
