package com.salonnbooking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.ReviewDtos;
import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.Review;
import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.ReviewRepository;
import com.salonnbooking.security.CurrentUserService;

import jakarta.persistence.EntityManager;

@Service
public class ReviewService extends BaseCrudService<Review> {

    private final ReviewRepository repository;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserService currentUserService;

    public ReviewService(
            ReviewRepository repository,
            AppointmentRepository appointmentRepository,
            CurrentUserService currentUserService,
            EntityManager entityManager) {
        super(repository, entityManager, Review.class);
        this.repository = repository;
        this.appointmentRepository = appointmentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ReviewDtos.ReviewResponse createMyReview(ReviewDtos.CreateReviewRequest request) {
        User customer = currentUserService.requireCurrentUser();
        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Only CUSTOMER can create reviews");
        }
        if (request.appointmentId() == null) {
            throw new IllegalArgumentException("appointmentId is required");
        }
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.appointmentId()));
        if (appointment.getCustomer() == null || !appointment.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Appointment not found with id: " + request.appointmentId());
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Only COMPLETED appointments can be reviewed");
        }
        if (repository.existsByAppointmentIdAndCustomerId(appointment.getId(), customer.getId())) {
            throw new IllegalArgumentException("Appointment was already reviewed");
        }

        Review review = Review.builder()
                .appointment(appointment)
                .customer(customer)
                .staff(appointment.getStaff())
                .rating(request.rating())
                .comment(request.comment())
                .createdAt(LocalDateTime.now())
                .build();
        return toResponse(repository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewDtos.ReviewResponse> getMyReviews() {
        User customer = currentUserService.requireCurrentUser();
        return repository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDtos.ReviewResponse> getReviews(Long appointmentId, Long staffId) {
        List<Review> reviews;
        if (appointmentId != null) {
            reviews = repository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
        } else if (staffId != null) {
            reviews = repository.findByStaffIdOrderByCreatedAtDesc(staffId);
        } else {
            reviews = repository.findAll();
        }
        return reviews.stream().map(this::toResponse).toList();
    }

    public ReviewDtos.ReviewResponse toResponse(Review review) {
        return new ReviewDtos.ReviewResponse(
                review.getId(),
                review.getAppointment() != null ? review.getAppointment().getId() : null,
                review.getCustomer() != null ? review.getCustomer().getId() : null,
                review.getCustomer() != null ? review.getCustomer().getFullName() : null,
                review.getStaff() != null ? review.getStaff().getId() : null,
                review.getStaff() != null ? review.getStaff().getFullName() : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}
