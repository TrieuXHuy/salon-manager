package com.salonnbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalonnBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalonnBookingApplication.class, args);
	}

}
