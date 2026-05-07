package com.salonnbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Salon Booking System API")
						.version("1.0.0")
						.description("API for managing salon appointments, payments, customers and services")
						.contact(new Contact()
								.name("Support Team")
								.email("support@salonn-booking.com")
								.url("https://www.salonn-booking.com"))
						.license(new License()
								.name("Apache 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0.html")))
				.addServersItem(new Server()
						.url("http://localhost:8080")
						.description("Development Server"));
	}
}
