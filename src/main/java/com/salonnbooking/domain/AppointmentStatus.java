package com.salonnbooking.domain;

public enum AppointmentStatus {
	pending("Chờ đặt cọc"),
	confirmed("Đã giữ chỗ"),
	in_progress("Đang phục vụ"),
	awaiting_payment("Chờ thanh toán"),
	completed("Hoàn thành"),
	cancelled("Đã hủy"),
	paid("Hoàn thành");

	private final String displayName;

	AppointmentStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
