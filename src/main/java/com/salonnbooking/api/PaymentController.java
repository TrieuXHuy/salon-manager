package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.domain.Payment;
import com.salonnbooking.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController extends BaseCrudController<Payment> {

    public PaymentController(PaymentService service) {
        super(service);
    }
}
