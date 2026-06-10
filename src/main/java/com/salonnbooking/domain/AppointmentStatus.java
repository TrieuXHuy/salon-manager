package com.salonnbooking.domain;

public enum AppointmentStatus {
	pending("Ch\u1edd c\u1ed1c"),
	confirmed("\u0110\u00e3 c\u1ed1c"),
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
