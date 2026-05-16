package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.domain.Payment;
import com.salonnbooking.repository.PaymentRepository;

import jakarta.persistence.EntityManager;

@Service
public class PaymentService extends BaseCrudService<Payment> {

    public PaymentService(PaymentRepository repository, EntityManager entityManager) {
        super(repository, entityManager, Payment.class);
    }
}
