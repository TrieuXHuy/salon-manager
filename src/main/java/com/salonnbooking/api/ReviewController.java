package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.domain.Review;
import com.salonnbooking.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController extends BaseCrudController<Review> {

    public ReviewController(ReviewService service) {
        super(service);
    }
}
