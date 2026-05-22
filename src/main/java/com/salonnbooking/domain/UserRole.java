package com.salonnbooking.domain;

public enum UserRole {
	OWNER("Chủ cửa hàng"),
	STAFF("Nhân viên"),
	CUSTOMER("Khách hàng");

	private final String displayName;

	UserRole(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
