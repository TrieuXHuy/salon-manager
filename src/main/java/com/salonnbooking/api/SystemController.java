package com.salonnbooking.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.SystemRequests;
import com.salonnbooking.repository.CustomerRepository;

@RestController
@RequestMapping("/api/system")
public class SystemController {
	@Value("${spring.application.name:Salon Booking System}")
	private String applicationName;

	@Value("${app.version:1.0.0}")
	private String appVersion;

	private final CustomerRepository customerRepository;
	private final long startTime = System.currentTimeMillis();

	public SystemController(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@GetMapping("/health")
	@ResponseStatus(HttpStatus.OK)
	public SystemRequests.HealthCheckResponse healthCheck() {
		try {
			customerRepository.count();
			return new SystemRequests.HealthCheckResponse(
					"UP",
					"System is running healthy",
					appVersion,
					LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		} catch (Exception e) {
			return new SystemRequests.HealthCheckResponse(
					"DOWN",
					"System error: " + e.getMessage(),
					appVersion,
					LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		}
	}

	@GetMapping("/info")
	@ResponseStatus(HttpStatus.OK)
	public SystemRequests.SystemInfoResponse getSystemInfo() {
		long uptime = (System.currentTimeMillis() - startTime) / 1000;
		String uptimeStr = String.format("%d days, %d hours, %d minutes",
				uptime / 86400,
				(uptime % 86400) / 3600,
				(uptime % 3600) / 60);

		return new SystemRequests.SystemInfoResponse(
				applicationName,
				appVersion,
				System.getProperty("java.version"),
				System.getProperty("spring.profiles.active", "default"),
				uptimeStr);
	}

	@GetMapping("/db-status")
	@ResponseStatus(HttpStatus.OK)
	public SystemRequests.DatabaseStatusResponse getDatabaseStatus() {
		try {
			long count = customerRepository.count();
			return new SystemRequests.DatabaseStatusResponse(
					true,
					"booking_system",
					"SQL Server",
					"Database connection is healthy");
		} catch (Exception e) {
			return new SystemRequests.DatabaseStatusResponse(
					false,
					"booking_system",
					"SQL Server",
					"Database connection failed: " + e.getMessage());
		}
	}
}
