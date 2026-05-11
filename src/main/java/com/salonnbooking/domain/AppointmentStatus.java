package com.salonnbooking.domain;

public enum AppointmentStatus {
	pending("Ch\u1edd x\u1eed l\u00fd"),
	confirmed("\u0110\u00e3 x\u00e1c nh\u1eadn"),
	completed("Ho\u00e0n th\u00e0nh"),
	cancelled("\u0110\u00e3 h\u1ee7y"),
	paid("\u0110\u00e3 thanh to\u00e1n");

	private final String displayName;

	AppointmentStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
