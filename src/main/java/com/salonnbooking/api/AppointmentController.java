package com.salonnbooking.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.service.BookingService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final BookingService bookingService;

    public AppointmentController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDtos.AppointmentResponse create(@RequestBody BookingDtos.CreateAppointmentRequest request) {
        return bookingService.createAppointment(request);
    }
}
