package com.salonnbooking;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class SalonnBookingApplicationTests {

	@Test
	void applicationCanBeConstructed() {
		assertDoesNotThrow(SalonnBookingApplication::new);
	}

}
