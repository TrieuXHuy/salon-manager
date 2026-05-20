package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.ReviewDtos;
import com.salonnbooking.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReviewDtos.ReviewResponse> findAll(
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long staffId) {
        return service.getReviews(appointmentId, staffId);
    }

    @GetMapping("/my")
    public List<ReviewDtos.ReviewResponse> findMine() {
        return service.getMyReviews();
    }

    @PostMapping
    public ReviewDtos.ReviewResponse create(@RequestBody ReviewDtos.CreateReviewRequest request) {
        return service.createMyReview(request);
    }
}
