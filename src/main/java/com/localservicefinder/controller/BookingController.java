package com.localservicefinder.controller;

import com.localservicefinder.dto.BookingRequest;
import com.localservicefinder.entity.Booking;
import com.localservicefinder.entity.User;
import com.localservicefinder.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    private User getCurrentAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Customer places a service booking
    @PostMapping
    public ResponseEntity<?> placeBooking(@Valid @RequestBody BookingRequest request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Booking booking = bookingService.createBooking(currentUser.getId(), request);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get specific booking details (accessible by creator, provider, or admin)
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingDetails(@PathVariable("id") Long id) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Booking booking = bookingService.getBookingById(id);
            
            // Validate roles and authorization to view
            boolean isUser = booking.getUser().getId().equals(currentUser.getId());
            boolean isProvider = booking.getService().getProvider().getId().equals(currentUser.getId());
            boolean isAdmin = "ADMIN".equals(currentUser.getRole());

            if (!isUser && !isProvider && !isAdmin) {
                return ResponseEntity.status(403).body("Access Denied");
            }

            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Customer cancels their booking
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable("id") Long id) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Booking cancelled = bookingService.updateBookingStatus(id, currentUser.getId(), "CANCELLED");
            return ResponseEntity.ok(cancelled);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
