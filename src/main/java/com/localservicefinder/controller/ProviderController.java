package com.localservicefinder.controller;

import com.localservicefinder.dto.ProviderProfileRequest;
import com.localservicefinder.entity.*;
import com.localservicefinder.service.BookingService;
import com.localservicefinder.service.RatingFeedbackService;
import com.localservicefinder.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ProviderController {

    @Autowired
    private ServiceProviderService providerService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RatingFeedbackService ratingFeedbackService;

    private User getCurrentAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/api/provider/profile")
    public ResponseEntity<?> getProfile() {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            ServiceProvider provider = providerService.getProfile(currentUser.getId());
            return ResponseEntity.ok(provider);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/api/provider/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProviderProfileRequest request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            ServiceProvider updated = providerService.updateProfile(currentUser.getId(), request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/api/provider/bookings")
    public ResponseEntity<List<Booking>> getBookings() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Booking> bookings = bookingService.getBookingsForProvider(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/api/provider/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable("id") Long bookingId,
            @RequestBody Map<String, String> request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            String status = request.get("status");
            Booking updated = bookingService.updateBookingStatus(bookingId, currentUser.getId(), status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/api/provider/ratings")
    public ResponseEntity<?> getRatings() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Rating> ratings = ratingFeedbackService.getRatingsForProvider(currentUser.getId());
        Double avgRating = ratingFeedbackService.getAverageRatingForProvider(currentUser.getId());

        Map<String, Object> stats = new HashMap<>();
        stats.put("ratings", ratings);
        stats.put("averageRating", avgRating);

        return ResponseEntity.ok(stats);
    }

    // Public endpoint: Allow any user to fetch public provider details (reviews, experience, bio) before booking
    @GetMapping("/api/providers/public/{id}")
    public ResponseEntity<?> getPublicProviderProfile(@PathVariable("id") UUID id) {
        try {
            ServiceProvider provider = providerService.getProfile(id);
            if (!provider.getIsApproved()) {
                return ResponseEntity.status(403).body("Provider account is pending verification and is not publicly listed yet.");
            }
            List<Rating> ratings = ratingFeedbackService.getRatingsForProvider(id);
            Double avgRating = ratingFeedbackService.getAverageRatingForProvider(id);

            Map<String, Object> details = new HashMap<>();
            
            // Clean user data representation for security (no password hash)
            Map<String, Object> publicUser = new HashMap<>();
            publicUser.put("fullName", provider.getUser().getFullName());
            publicUser.put("email", provider.getUser().getEmail());
            publicUser.put("phone", provider.getUser().getPhone());
            publicUser.put("profileImage", provider.getUser().getProfileImage());

            details.put("user", publicUser);
            details.put("businessName", provider.getBusinessName());
            details.put("bio", provider.getBio());
            details.put("address", provider.getAddress());
            details.put("city", provider.getCity());
            details.put("yearsOfExperience", provider.getYearsOfExperience());
            details.put("whatsappNumber", provider.getWhatsappNumber());
            details.put("ratings", ratings);
            details.put("averageRating", avgRating);

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
