package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.domain.Review;
import com.salonnbooking.repository.ReviewRepository;

import jakarta.persistence.EntityManager;

@Service
public class ReviewService extends BaseCrudService<Review> {

    public ReviewService(ReviewRepository repository, EntityManager entityManager) {
        super(repository, entityManager, Review.class);
    }
}
