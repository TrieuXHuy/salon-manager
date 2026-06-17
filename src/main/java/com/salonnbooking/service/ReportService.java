package com.salonnbooking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ReportRequests;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.PaymentRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {
	private final AppointmentRepository appointmentRepository;
	private final PaymentRepository paymentRepository;

	public ReportService(AppointmentRepository appointmentRepository, PaymentRepository paymentRepository) {
		this.appointmentRepository = appointmentRepository;
		this.paymentRepository = paymentRepository;
	}

	public List<ReportRequests.DailyRevenueResponse> getDailyRevenueReport(LocalDate startDate, LocalDate endDate) {
		// Lấy tất cả lịch hẹn trong khoảng ngày cần báo cáo.
		List<Appointment> appointments = appointmentRepository
				.findAppointmentsBetween(
						startDate.atStartOfDay(),
						endDate.atTime(23, 59, 59));

		// Gom lịch hẹn theo từng ngày để tính báo cáo riêng.
		Map<LocalDate, List<Appointment>> groupedByDate = appointments.stream()
				.collect(Collectors.groupingBy(a -> a.getAppointmentTime().toLocalDate()));

		// Danh sách kết quả trả về cho report.
		List<ReportRequests.DailyRevenueResponse> reports = new ArrayList<>();

		// Duyệt từng ngày trong khoảng báo cáo.
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			// Lấy các lịch hẹn của riêng ngày đó.
			List<Appointment> dayAppointments = groupedByDate.getOrDefault(date, new ArrayList<>());
			// Đếm số lịch đã hoàn thành.
			int completedCount = (int) dayAppointments.stream()
					.filter(a -> a.getStatus() == AppointmentStatus.completed)
					.count();

			// Cộng doanh thu từ tất cả payment đã thanh toán trong ngày.
			BigDecimal totalRevenue = BigDecimal.ZERO;
			for (Appointment apt : dayAppointments) {
				List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
				for (Payment payment : payments) {
					if (payment.getPaymentStatus() == PaymentStatus.paid) {
						totalRevenue = totalRevenue.add(payment.getAmount());
					}
				}
			}

			// Tạo một dòng report cho ngày hiện tại.
			reports.add(new ReportRequests.DailyRevenueResponse(
					date,
					totalRevenue,
					dayAppointments.size(),
					completedCount));
		}

		return reports;
	}

	public List<ReportRequests.ServiceRevenueResponse> getServiceRevenueReport() {
		// Lấy toàn bộ lịch hẹn để gom theo từng dịch vụ.
		List<Appointment> allAppointments = appointmentRepository.findAll();

		// Gom các lịch hẹn theo serviceId.
		Map<Integer, List<Appointment>> groupedByService = allAppointments.stream()
				.collect(Collectors.groupingBy(a -> a.getService().getId()));

		// Danh sách kết quả báo cáo.
		List<ReportRequests.ServiceRevenueResponse> reports = new ArrayList<>();

		// Tính doanh thu riêng cho từng dịch vụ.
		for (Map.Entry<Integer, List<Appointment>> entry : groupedByService.entrySet()) {
			List<Appointment> serviceAppointments = entry.getValue();
			// Số lượng lịch hẹn của dịch vụ này.
			int appointmentCount = serviceAppointments.size();

			// Cộng doanh thu từ các payment đã thanh toán.
			BigDecimal totalRevenue = BigDecimal.ZERO;
			for (Appointment apt : serviceAppointments) {
				List<Payment> payments = paymentRepository.findByAppointmentId(apt.getId());
				for (Payment payment : payments) {
					if (payment.getPaymentStatus() == PaymentStatus.paid) {
						totalRevenue = totalRevenue.add(payment.getAmount());
					}
				}
			}

			// Doanh thu trung bình trên mỗi lịch hẹn.
			BigDecimal avgRevenue = appointmentCount > 0
					? totalRevenue.divide(BigDecimal.valueOf(appointmentCount), 2, java.math.RoundingMode.HALF_UP)
					: BigDecimal.ZERO;

			// Tạo một dòng report cho dịch vụ hiện tại.
			reports.add(new ReportRequests.ServiceRevenueResponse(
					entry.getKey(),
					serviceAppointments.get(0).getService().getName(),
					appointmentCount,
					totalRevenue,
					avgRevenue));
		}

		// Sắp xếp dịch vụ có doanh thu cao nhất lên trước.
		return reports.stream()
				.sorted((a, b) -> b.totalRevenue().compareTo(a.totalRevenue()))
				.collect(Collectors.toList());
	}

	public List<ReportRequests.PaymentMethodResponse> getPaymentMethodReport() {
		// Lấy toàn bộ payment để thống kê theo phương thức.
		List<Payment> allPayments = paymentRepository.findAll();

		// Chỉ lấy các payment đã thanh toán và gom theo paymentMethod.
		Map<PaymentMethod, List<Payment>> groupedByMethod = allPayments.stream()
				.filter(p -> p.getPaymentStatus() == PaymentStatus.paid)
				.collect(Collectors.groupingBy(Payment::getPaymentMethod));

		// Tổng số payment đã thanh toán.
		int totalPayments = allPayments.stream()
				.filter(p -> p.getPaymentStatus() == PaymentStatus.paid)
				.toList()
				.size();

		// Tổng số tiền đã thu.
		BigDecimal totalAmount = allPayments.stream()
				.filter(p -> p.getPaymentStatus() == PaymentStatus.paid)
				.map(Payment::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// Danh sách kết quả báo cáo.
		List<ReportRequests.PaymentMethodResponse> reports = new ArrayList<>();

		// Tính số lượng, tổng tiền và tỷ lệ cho từng phương thức.
		for (Map.Entry<PaymentMethod, List<Payment>> entry : groupedByMethod.entrySet()) {
			List<Payment> methodPayments = entry.getValue();
			// Số payment của phương thức này.
			int count = methodPayments.size();

			// Tổng tiền thu được từ phương thức này.
			BigDecimal methodTotal = methodPayments.stream()
					.map(Payment::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			// Tỷ lệ đóng góp của phương thức này trên tổng doanh thu.
			BigDecimal percentage = totalPayments > 0
					? methodTotal.divide(totalAmount, 2, java.math.RoundingMode.HALF_UP)
							.multiply(BigDecimal.valueOf(100))
					: BigDecimal.ZERO;

			// Tạo một dòng report cho phương thức thanh toán hiện tại.
			reports.add(new ReportRequests.PaymentMethodResponse(
					entry.getKey().toString(),
					count,
					methodTotal,
					percentage));
		}

		return reports;
	}

	public ReportRequests.AppointmentStatsResponse getAppointmentStats() {
		// Lấy toàn bộ lịch hẹn để đếm theo trạng thái.
		List<Appointment> allAppointments = appointmentRepository.findAll();

		// Trả về số lượng lịch hẹn tổng và số lượng theo từng trạng thái chính.
		return new ReportRequests.AppointmentStatsResponse(
				allAppointments.size(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.pending).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.confirmed).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.completed).count(),
				(int) allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.cancelled).count());
	}
}
