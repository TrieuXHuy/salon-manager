package com.salonnbooking.domain;

public enum AppointmentStatus {
	PENDING,
	CONFIRMED,
	CHECKED_IN,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED,
	NO_SHOW;

	public String getLabel() {
		return switch (this) {
			case PENDING -> "Moi tao";
			case CONFIRMED -> "Da xac nhan";
			case CHECKED_IN -> "Da check-in";
			case IN_PROGRESS -> "Dang thuc hien";
			case COMPLETED -> "Hoan thanh";
			case CANCELLED -> "Da huy";
			case NO_SHOW -> "Khong den";
		};
	}
}
