package com.salonnbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SalonnBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalonnBookingApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartup(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        String contextPath = env.getProperty("server.servlet.context-path", "");
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  Salon Booking API is running");
        System.out.println("  Base URL : http://localhost:" + port + contextPath);
        System.out.println("  Swagger  : http://localhost:" + port + contextPath + "/swagger-ui/index.html");
        System.out.println("==================================================");
        System.out.println();
    }

}
