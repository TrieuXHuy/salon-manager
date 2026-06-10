package com.salonnbooking.domain;

public enum AppointmentStatus {
	pending("Chờ cọc"),
	confirmed("Đã cọc"),
	completed("\u0110\u00e3 l\u00e0m xong"),
	cancelled("\u0110\u00e3 h\u1ee7y"),
	paid("\u0110\u00e3 thanh to\u00e1n \u0111\u1ee7");

	private final String displayName;

	AppointmentStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
