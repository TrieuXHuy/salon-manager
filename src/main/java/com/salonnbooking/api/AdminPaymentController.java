package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.service.PaymentQueryService;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final PaymentQueryService paymentQueryService;

    public AdminPaymentController(PaymentQueryService paymentQueryService) {
        this.paymentQueryService = paymentQueryService;
    }

    @GetMapping
    public List<BookingDtos.PaymentResponse> getAllPayments() {
        return paymentQueryService.getAllPayments();
    }
}
