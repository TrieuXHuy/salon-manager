package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.service.BookingService;
import com.salonnbooking.service.PaymentQueryService;

@RestController
@RequestMapping("/api/customer/appointments")
public class CustomerAppointmentController {

    private final BookingService bookingService;
    private final PaymentQueryService paymentQueryService;

    public CustomerAppointmentController(BookingService bookingService, PaymentQueryService paymentQueryService) {
        this.bookingService = bookingService;
        this.paymentQueryService = paymentQueryService;
    }

    @GetMapping
    public List<BookingDtos.AppointmentResponse> findMine(@RequestParam(required = false) AppointmentStatus status) {
        return bookingService.getMyAppointments(status);
    }

    @GetMapping("/{id}")
    public BookingDtos.AppointmentResponse findMineById(@PathVariable Long id) {
        return bookingService.getMyAppointmentDetail(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingDtos.AppointmentResponse cancel(
            @PathVariable Long id,
            @RequestBody BookingDtos.CancelAppointmentRequest request) {
        return bookingService.cancelMyAppointment(id, request);
    }

    @GetMapping("/{id}/payment")
    public List<BookingDtos.PaymentResponse> getPayment(@PathVariable Long id) {
        return paymentQueryService.getMyAppointmentPayments(id);
    }
}
