package com.salonnbooking.service;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.services.emails.model.*;
import com.salonnbooking.domain.Appointment;

@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final Resend resend;
	private final String fromEmail;

	public EmailService(
			@Value("${resend.api-key}") String apiKey,
			@Value("${resend.from-email}") String fromEmail) {
		this.resend = new Resend(apiKey);
		this.fromEmail = fromEmail;
	}

	public void sendBookingConfirmation(Appointment appointment) {
		if (appointment == null || appointment.getCustomer() == null) {
			return;
		}

		String toEmail = appointment.getCustomer().getEmail();
		if (toEmail == null || toEmail.trim().isEmpty()) {
			log.info("Customer has no email address. Skipping email confirmation for appointment ID: {}", appointment.getId());
			return;
		}

		// Run asynchronously to avoid blocking the caller thread
		CompletableFuture.runAsync(() -> {
			try {
				log.info("Sending booking confirmation email to {} for appointment ID: {}", toEmail, appointment.getId());
				
				String htmlContent = buildConfirmationHtml(appointment);
				
				CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
						.from(fromEmail)
						.to(toEmail)
						.subject("Xác nhận đặt lịch hẹn thành công - Salon Booking")
						.html(htmlContent)
						.build();

				CreateEmailResponse data = resend.emails().send(sendEmailRequest);
				log.info("Email sent successfully. Email ID: {}", data.getId());
			} catch (Exception e) {
				log.error("Failed to send booking confirmation email for appointment ID: {}", appointment.getId(), e);
			}
		});
	}

	private String buildConfirmationHtml(Appointment appointment) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		String formattedTime = appointment.getAppointmentTime() != null 
				? appointment.getAppointmentTime().format(formatter) 
				: "N/A";
		
		String customerName = appointment.getCustomer().getFullName();
		String serviceName = appointment.getService() != null ? appointment.getService().getName() : "N/A";
		String price = appointment.getService() != null && appointment.getService().getPrice() != null
				? String.format("%,.0f VNĐ", appointment.getService().getPrice())
				: "N/A";
		String roomName = appointment.getRoom() != null ? appointment.getRoom().getName() : "N/A";
		String note = appointment.getNote() != null && !appointment.getNote().trim().isEmpty() 
				? appointment.getNote() 
				: "Không có";

		return "<!DOCTYPE html>"
				+ "<html>"
				+ "<head>"
				+ "    <meta charset='UTF-8'>"
				+ "    <title>Xác nhận đặt lịch</title>"
				+ "</head>"
				+ "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 20px; color: #333;'>"
				+ "    <div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); border: 1px solid #e0e0e0;'>"
				+ "        <!-- Header -->"
				+ "        <div style='background: linear-gradient(135deg, #6a11cb, #2575fc); padding: 30px; text-align: center; color: white;'>"
				+ "            <h1 style='margin: 0; font-size: 24px; font-weight: bold; letter-spacing: 0.5px;'>Salon Booking System</h1>"
				+ "            <p style='margin: 5px 0 0; font-size: 14px; opacity: 0.9;'>Xác nhận lịch hẹn của bạn</p>"
				+ "        </div>"
				+ "        "
				+ "        <!-- Content -->"
				+ "        <div style='padding: 30px; line-height: 1.6;'>"
				+ "            <h2 style='color: #2575fc; margin-top: 0;'>Xin chào " + customerName + ",</h2>"
				+ "            <p>Cảm ơn bạn đã lựa chọn dịch vụ của chúng tôi. Lịch hẹn của bạn đã được đặt thành công. Dưới đây là thông tin chi tiết lịch hẹn:</p>"
				+ "            "
				+ "            <!-- Summary Card -->"
				+ "            <div style='background-color: #f8f9fa; border-left: 4px solid #2575fc; padding: 20px; border-radius: 6px; margin: 25px 0;'>"
				+ "                <table style='width: 100%; border-collapse: collapse;'>"
				+ "                    <tr>"
				+ "                        <td style='padding: 6px 0; font-weight: bold; color: #555; width: 150px;'>Mã lịch hẹn:</td>"
				+ "                        <td style='padding: 6px 0; color: #333;'>#" + appointment.getId() + "</td>"
				+ "                    </tr>"
				+ "                    <tr>"
				+ "                        <td style='padding: 6px 0; font-weight: bold; color: #555;'>Thời gian:</td>"
				+ "                        <td style='padding: 6px 0; color: #333; font-weight: bold;'>" + formattedTime + "</td>"
				+ "                    </tr>"
				+ "                    <tr>"
				+ "                        <td style='padding: 6px 0; font-weight: bold; color: #555;'>Dịch vụ:</td>"
				+ "                        <td style='padding: 6px 0; color: #333;'>" + serviceName + "</td>"
				+ "                    </tr>"
				+ "                    <tr>"
				+ "                        <td style='padding: 6px 0; font-weight: bold; color: #555;'>Giá tiền:</td>"
				+ "                        <td style='padding: 6px 0; color: #e63946; font-weight: bold;'>" + price + "</td>"
				+ "                    </tr>"
				+ "                    <tr>"
				+ "                        <td style='padding: 6px 0; font-weight: bold; color: #555;'>Phòng dịch vụ:</td>"
				+ "                        <td style='padding: 6px 0; color: #333;'>" + roomName + "</td>"
				+ "                    </tr>"
				+ "                    <tr>"
				+ "                        <td style='padding: 6px 0; font-weight: bold; color: #555;'>Ghi chú:</td>"
				+ "                        <td style='padding: 6px 0; color: #666; font-style: italic;'>" + note + "</td>"
				+ "                    </tr>"
				+ "                </table>"
				+ "            </div>"
				+ "            "
				+ "            <p style='margin-bottom: 0;'>Nếu bạn muốn thay đổi hoặc hủy lịch hẹn, vui lòng liên hệ với chúng tôi qua số điện thoại của cửa hàng ít nhất 2 tiếng trước giờ hẹn.</p>"
				+ "        </div>"
				+ "        "
				+ "        <!-- Footer -->"
				+ "        <div style='background-color: #f4f4f9; padding: 20px; text-align: center; font-size: 12px; color: #777; border-top: 1px solid #e0e0e0;'>"
				+ "            <p style='margin: 0;'>Đây là email tự động từ hệ thống Salon Booking. Vui lòng không trả lời trực tiếp email này.</p>"
				+ "            <p style='margin: 5px 0 0;'>&copy; 2026 Salon Booking System. All rights reserved.</p>"
				+ "        </div>"
				+ "    </div>"
				+ "</body>"
				+ "</html>";
	}
}
