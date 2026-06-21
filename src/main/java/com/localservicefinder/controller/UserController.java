package com.localservicefinder.controller;

import com.localservicefinder.entity.*;
import com.localservicefinder.service.BookingService;
import com.localservicefinder.service.RatingFeedbackService;
import com.localservicefinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RatingFeedbackService ratingFeedbackService;

    private User getCurrentAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        User currentUser = getCurrentAuthenticatedUser();
        return ResponseEntity.ok(userService.findById(currentUser.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            String fullName = request.get("fullName");
            String phone = request.get("phone");
            String profileImage = request.get("profileImage");

            User updated = userService.updateUserProfile(currentUser.getId(), fullName, phone, profileImage);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getMyBookings() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Booking> bookings = bookingService.getBookingsForUser(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/bookings/{id}/rate")
    public ResponseEntity<?> rateBooking(
            @PathVariable("id") Long bookingId,
            @RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Integer ratingValue = (Integer) request.get("ratingValue");
            String comments = (String) request.get("comments");

            Rating rating = ratingFeedbackService.addRating(currentUser.getId(), bookingId, ratingValue, comments);
            return ResponseEntity.ok(rating);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, String> request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            String subject = request.get("subject");
            String message = request.get("message");

            Feedback feedback = ratingFeedbackService.submitFeedback(currentUser.getId(), subject, message);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
