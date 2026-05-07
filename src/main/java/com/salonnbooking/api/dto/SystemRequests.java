package com.salonnbooking.api.dto;

public final class SystemRequests {
	private SystemRequests() {
	}

	public record HealthCheckResponse(
			String status,
			String message,
			String version,
			String timestamp) {
	}

	public record SystemInfoResponse(
			String applicationName,
			String version,
			String javaVersion,
			String environment,
			String uptime) {
	}

	public record DatabaseStatusResponse(
			Boolean connected,
			String databaseName,
			String databaseVersion,
			String message) {
	}
}
