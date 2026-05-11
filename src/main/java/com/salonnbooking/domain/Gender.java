package com.salonnbooking.domain;

public enum Gender {
	male("Nam"),
	female("N\u1eef"),
	other("Kh\u00e1c");

	private final String displayName;

	Gender(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
