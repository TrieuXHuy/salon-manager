package com.salonnbooking.domain;

public enum Role {
	ADMIN,
	RECEPTIONIST,
	EMPLOYEE;

	public String getLabel() {
		return switch (this) {
			case ADMIN -> "Admin";
			case RECEPTIONIST -> "Le tan";
			case EMPLOYEE -> "Nhan vien";
		};
	}
}
