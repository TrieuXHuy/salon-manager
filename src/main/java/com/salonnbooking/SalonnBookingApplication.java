package com.salonnbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SalonnBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalonnBookingApplication.class, args);
	}

}
