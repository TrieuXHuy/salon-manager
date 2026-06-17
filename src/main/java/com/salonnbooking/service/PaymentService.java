package com.salonnbooking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.PaymentRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentStage;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.domain.ServiceEntity;
import com.salonnbooking.domain.ServiceRoom;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.PaymentRepository;
import com.salonnbooking.repository.ServiceRoomRepository;

@Service
@Transactional
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final AppointmentRepository appointmentRepository;
	private final ServiceRoomRepository serviceRoomRepository;
	private final EmailService emailService;

	public PaymentService(PaymentRepository paymentRepository, AppointmentRepository appointmentRepository,
			ServiceRoomRepository serviceRoomRepository, EmailService emailService) {
		this.paymentRepository = paymentRepository;
		this.appointmentRepository = appointmentRepository;
		this.serviceRoomRepository = serviceRoomRepository;
		this.emailService = emailService;
	}

	@Transactional(readOnly = true)
	public List<Payment> findAll() {
		return paymentRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Payment findById(Integer id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
	}

	public Payment save(PaymentRequests.Create req) {
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + req.appointmentId()));
		// Nếu paymentStage không được cung cấp, tự động suy luận dựa trên số tiền và trạng thái của lịch hẹn.
		PaymentStage stage = req.paymentStage() == null ? inferStage(appointment, normalize(req.amount())) : req.paymentStage();
		// Xác định số tiền dự kiến dựa trên stage.
		BigDecimal expectedAmount = expectedAmount(appointment, stage);
		validateAmount(stage, req.amount(), expectedAmount);
		// Kiểm tra tính hợp lệ của lịch hẹn đối với stage thanh toán hiện tại.
		validateAppointmentForStage(appointment, stage);

		Payment payment = new Payment();
		payment.setAppointment(appointment);
		payment.setAmount(normalize(req.amount()));
		payment.setPaymentMethod(req.paymentMethod());
		payment.setPaymentStage(stage);
		payment.setPaymentStatus(PaymentStatus.paid);
		payment.setPaidAt(req.paidAt() != null ? req.paidAt() : LocalDateTime.now());

		Payment saved = paymentRepository.save(payment);
		applyPaymentEffects(appointment, saved);
		return saved;
	}

	public Payment update(Integer id, PaymentRequests.Update req) {
		Payment payment = findById(id);
		Appointment appointment = appointmentRepository.findById(req.appointmentId())
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + req.appointmentId()));
		PaymentStage stage = req.paymentStage() == null ? inferStage(appointment, normalize(req.amount())) : req.paymentStage();
		BigDecimal expectedAmount = expectedAmount(appointment, stage);
		validateAmount(stage, req.amount(), expectedAmount);

		payment.setAppointment(appointment);
		payment.setAmount(normalize(req.amount()));
		payment.setPaymentMethod(req.paymentMethod());
		payment.setPaymentStage(stage);
		payment.setPaymentStatus(req.paymentStatus());
		payment.setPaidAt(req.paidAt());

		Payment saved = paymentRepository.save(payment);
		recalculateAppointmentFinancials(appointment);
		updateAppointmentStatusAfterPayment(appointment);
		appointmentRepository.save(appointment);
		return saved;
	}

	public void delete(Integer id) {
		if (!paymentRepository.existsById(id)) {
			throw new ResourceNotFoundException("Payment not found with id: " + id);
		}
		paymentRepository.deleteById(id);
	}

	public Payment markAsPaid(Integer id) {
		Payment payment = findById(id);
		payment.setPaymentStatus(PaymentStatus.paid);
		payment.setPaidAt(LocalDateTime.now());
		Payment saved = paymentRepository.save(payment);
		applyPaymentEffects(saved.getAppointment(), saved);
		return saved;
	}

	private void applyPaymentEffects(Appointment appointment, Payment payment) {
		// Cập nhật lại tiền của appointment sau khi thêm payment mới.
		recalculateAppointmentFinancials(appointment);
		if (payment.getPaymentStage() == PaymentStage.deposit) {
			// Thu cọc thì giữ phòng và xác nhận lịch.
			lockRoomForAppointment(appointment);
			if (appointment.getStatus() == AppointmentStatus.pending) {
				appointment.setStatus(AppointmentStatus.confirmed);
			}
			// Gửi email xác nhận booking sau khi cọc thành công.
			emailService.sendBookingConfirmation(appointment);
		}
		// Đồng bộ trạng thái appointment sau thanh toán.
		updateAppointmentStatusAfterPayment(appointment);
		appointmentRepository.save(appointment);
	}

	private void updateAppointmentStatusAfterPayment(Appointment appointment) {
		// Tính lại tiền trước khi quyết định đổi trạng thái.
		recalculateAppointmentFinancials(appointment);
		// Nếu đã thanh toán đủ và có phát sinh tiền đã trả thì chuyển sang completed.
		if (safeAmount(appointment.getRemainingAmount()).compareTo(BigDecimal.ZERO) <= 0
				&& appointment.getAmountPaid() != null
				&& appointment.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
			AppointmentStatus previousStatus = appointment.getStatus();
			appointment.setStatus(AppointmentStatus.completed);
			// Chỉ cộng điểm khi đây là lần hoàn tất thực sự, không phải trạng thái đã hoàn tất trước đó.
			if (previousStatus != AppointmentStatus.completed && previousStatus != AppointmentStatus.paid) {
				int earnedPoints = calculateLoyaltyPoints(appointment);
				// Cộng điểm tích lũy cho customer.
				appointment.getCustomer().setLoyaltyPoints(
						appointment.getCustomer().getLoyaltyPoints() + earnedPoints);
			}
		}
	}

	private void validateAppointmentForStage(Appointment appointment, PaymentStage stage) {
		if (stage == PaymentStage.deposit) {
			// Thu cọc: lịch hẹn phải còn ở trạng thái pending.
			if (appointment.getStatus() != AppointmentStatus.pending) {
				throw new IllegalArgumentException("Appointment has already been deposited");
			}
			return;
		}
		if (stage == PaymentStage.balance) {
			// Thu phần còn lại: lịch hẹn phải đang chờ thanh toán.
			if (appointment.getStatus() != AppointmentStatus.awaiting_payment) {
				throw new IllegalArgumentException("The appointment must be waiting for payment before collecting the remaining balance");
			}
			// Nếu đã trả đủ thì không cho thu tiếp.
			BigDecimal remaining = safeAmount(appointment.getRemainingAmount());
			if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("Appointment has already been fully paid");
			}
		}
	}

	private PaymentStage inferStage(Appointment appointment, BigDecimal amount) {
		BigDecimal total = normalize(appointment.getService() == null ? BigDecimal.ZERO : appointment.getService().getPrice());
		BigDecimal deposit = total.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);
		BigDecimal remaining = normalize(total.subtract(normalize(appointment.getAmountPaid())));
		if (amount.compareTo(deposit) == 0) {
			return PaymentStage.deposit;
		}
		if (amount.compareTo(total) == 0 || amount.compareTo(remaining) == 0) {
			return PaymentStage.balance;
		}
		if (normalize(appointment.getAmountPaid()).compareTo(BigDecimal.ZERO) > 0) {
			return PaymentStage.balance;
		}
		return amount.compareTo(deposit) > 0 ? PaymentStage.balance : PaymentStage.deposit;
	}

	private void lockRoomForAppointment(Appointment appointment) {
		if (appointment.getAppointmentTime() == null) {
			throw new IllegalArgumentException("Appointment time is required");
		}

		ServiceRoom chosenRoom = appointment.getRoom();
		if (chosenRoom != null) {
			if (!isRoomAvailable(appointment, chosenRoom)) {
				throw new IllegalArgumentException(chosenRoom.getName() + " is already booked in this time slot");
			}
			return;
		}

		for (ServiceRoom room : serviceRoomRepository.findByIsActiveTrueOrderByIdAsc()) {
			if (isRoomAvailable(appointment, room)) {
				appointment.setRoom(room);
				return;
			}
		}
		throw new IllegalArgumentException("No service room is available in this time slot");
	}

	private boolean isRoomAvailable(Appointment appointment, ServiceRoom room) {
		LocalDateTime startTime = appointment.getAppointmentTime();
		int duration = roundedDuration(appointment.getService() == null ? null : appointment.getService().getDurationMinutes());
		LocalDateTime endTime = startTime.plusMinutes(duration);
		LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay();
		LocalDateTime dayEnd = startTime.toLocalDate().atTime(23, 59, 59);
		for (Appointment existing : appointmentRepository.findAppointmentsBetween(dayStart, dayEnd)) {
			if (existing.getId().equals(appointment.getId())) {
				continue;
			}
			if (!isBlockingStatus(existing.getStatus())) {
				continue;
			}
			if (existing.getRoom() == null || !existing.getRoom().getId().equals(room.getId())) {
				continue;
			}
			LocalDateTime existingStart = existing.getAppointmentTime();
			int existingDuration = roundedDuration(existing.getService() == null ? null : existing.getService().getDurationMinutes());
			LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);
			boolean overlap = startTime.isBefore(existingEnd) && endTime.isAfter(existingStart);
			if (overlap) {
				return false;
			}
		}
		return true;
	}

	private boolean isBlockingStatus(AppointmentStatus status) {
		return status == AppointmentStatus.confirmed
				|| status == AppointmentStatus.in_progress
				|| status == AppointmentStatus.awaiting_payment
				|| status == AppointmentStatus.completed
				|| status == AppointmentStatus.paid;
	}

	private void recalculateAppointmentFinancials(Appointment appointment) {
		// Tổng tiền của lịch hẹn dựa trên giá service.
		BigDecimal total = normalize(appointment.getService() == null ? BigDecimal.ZERO : appointment.getService().getPrice());
		// Tiền cọc mặc định bằng 20% tổng tiền.
		BigDecimal deposit = total.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);
		// Cộng tất cả payment đã thanh toán của lịch hẹn này.
		BigDecimal paid = paymentRepository.findByAppointmentId(appointment.getId()).stream()
				.filter(payment -> payment.getPaymentStatus() == PaymentStatus.paid)
				.map(payment -> normalize(payment.getAmount()))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);
		// Không cho tiền đã trả vượt quá tổng tiền.
		paid = paid.min(total).setScale(2, RoundingMode.HALF_UP);
		// Cập nhật lại snapshot tiền vào appointment.
		appointment.setTotalAmount(total);
		appointment.setDepositAmount(deposit);
		appointment.setAmountPaid(paid);
		appointment.setRemainingAmount(normalize(total.subtract(paid)));
	}

	private BigDecimal expectedAmount(Appointment appointment, PaymentStage stage) {
		// Tổng tiền của lịch hẹn dựa trên giá service.
		BigDecimal total = normalize(appointment.getService() == null ? BigDecimal.ZERO : appointment.getService().getPrice());
		// Tiền cọc mặc định là 20% tổng tiền.
		BigDecimal deposit = total.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);
		// Số tiền đã trả trước đó.
		BigDecimal paid = normalize(appointment.getAmountPaid());
		// Phần tiền còn lại chưa thanh toán.
		BigDecimal remaining = normalize(total.subtract(paid));
		// Nếu đang thu cọc thì trả về deposit, ngược lại trả về phần còn lại.
		return stage == PaymentStage.deposit ? deposit : remaining;
	}

	private void validateAmount(PaymentStage stage, BigDecimal amount, BigDecimal expectedAmount) {
		// Chuẩn hóa số tiền trước khi so sánh.
		BigDecimal normalizedAmount = normalize(amount);
		BigDecimal normalizedExpected = normalize(expectedAmount);
		// Nếu amount khác số tiền hệ thống mong đợi thì chặn lại.
		if (normalizedAmount.compareTo(normalizedExpected) != 0) {
			throw new IllegalArgumentException("Invalid " + stage + " amount. Expected " + normalizedExpected);
		}
	}

	private BigDecimal normalize(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal safeAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
	}

	private int roundedDuration(Integer durationMinutes) {
		int duration = durationMinutes == null || durationMinutes <= 0 ? 30 : durationMinutes;
		return ((duration + 29) / 30) * 30;
	}

	private int calculateLoyaltyPoints(Appointment appointment) {
		ServiceEntity service = appointment.getService();
		BigDecimal price = service == null ? BigDecimal.ZERO : service.getPrice();
		if (price == null) {
			return 0;
		}
		return price.divide(BigDecimal.valueOf(10000), 0, RoundingMode.DOWN).intValue();
	}
}
