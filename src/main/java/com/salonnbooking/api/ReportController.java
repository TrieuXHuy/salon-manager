package com.salonnbooking.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.ReportRequests;
import com.salonnbooking.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	/** Báo cáo doanh thu theo ngày trong khoảng thời gian truyền vào. */
	@GetMapping("/daily-revenue")
	@ResponseStatus(HttpStatus.OK)
	public List<ReportRequests.DailyRevenueResponse> getDailyRevenueReport(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return reportService.getDailyRevenueReport(startDate, endDate);
	}

	/** Báo cáo doanh thu theo dịch vụ. */
	@GetMapping("/service-revenue")
	@ResponseStatus(HttpStatus.OK)
	public List<ReportRequests.ServiceRevenueResponse> getServiceRevenueReport() {
		return reportService.getServiceRevenueReport();
	}

	/** Báo cáo doanh thu theo phương thức thanh toán. */
	@GetMapping("/payment-methods")
	@ResponseStatus(HttpStatus.OK)
	public List<ReportRequests.PaymentMethodResponse> getPaymentMethodReport() {
		return reportService.getPaymentMethodReport();
	}

	/** Báo cáo thống kê tổng hợp về lịch hẹn. */
	@GetMapping("/appointment-stats")
	@ResponseStatus(HttpStatus.OK)
	public ReportRequests.AppointmentStatsResponse getAppointmentStats() {
		return reportService.getAppointmentStats();
	}
}
