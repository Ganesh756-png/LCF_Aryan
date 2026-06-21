package com.localservicefinder.service;

import com.localservicefinder.entity.*;
import com.localservicefinder.repository.BookingRepository;
import com.localservicefinder.repository.FeedbackRepository;
import com.localservicefinder.repository.RatingRepository;
import com.localservicefinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RatingFeedbackService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Rating addRating(UUID userId, Long bookingId, Integer ratingValue, String comments) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Enforce review policy: user must own booking and booking must be COMPLETED
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You did not make this booking");
        }

        if (!"COMPLETED".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot rate a service before it has been marked as COMPLETED");
        }

        // Check if rating already exists for this booking to avoid duplicates
        Optional<Rating> existing = ratingRepository.findByBookingId(bookingId);
        if (existing.isPresent()) {
            throw new RuntimeException("You have already submitted a rating for this service booking");
        }

        Rating rating = new Rating();
        rating.setBooking(booking);
        rating.setUser(booking.getUser());
        rating.setProvider(booking.getService().getProvider());
        rating.setRatingValue(ratingValue);
        rating.setComments(comments);

        return ratingRepository.save(rating);
    }

    public List<Rating> getRatingsForProvider(UUID providerId) {
        return ratingRepository.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    public Double getAverageRatingForProvider(UUID providerId) {
        return ratingRepository.findAverageRatingByProviderId(providerId);
    }

    @Transactional
    public Feedback submitFeedback(UUID userId, String subject, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setSubject(subject);
        feedback.setMessage(message);
        feedback.setStatus("PENDING");

        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Feedback resolveFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback ticket not found"));
        feedback.setStatus("RESOLVED");
        return feedbackRepository.save(feedback);
    }
}
