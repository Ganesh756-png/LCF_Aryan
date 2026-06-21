package com.localservicefinder.service;

import com.localservicefinder.dto.BookingRequest;
import com.localservicefinder.entity.Booking;
import com.localservicefinder.entity.Service;
import com.localservicefinder.entity.User;
import com.localservicefinder.repository.BookingRepository;
import com.localservicefinder.repository.ServiceRepository;
import com.localservicefinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Transactional
    public Booking createBooking(UUID userId, BookingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service listing not found"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setService(service);
        booking.setBookingDate(request.getBookingDate());
        booking.setStatus("PENDING");
        booking.setNotes(request.getNotes());
        booking.setTotalPrice(service.getPriceRate()); // standard base price rate

        booking = bookingRepository.save(booking);

        // Notify provider via email
        sendEmailNotification(booking, "CREATED");

        return booking;
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, UUID actorId, String newStatus) {
        Booking booking = getBookingById(bookingId);
        newStatus = newStatus.toUpperCase();

        boolean isUser = booking.getUser().getId().equals(actorId);
        boolean isProvider = booking.getService().getProvider().getId().equals(actorId);

        if (!isUser && !isProvider) {
            // Check if actor is Admin
            User actor = userRepository.findById(actorId)
                    .orElseThrow(() -> new RuntimeException("Actor not found"));
            if (!"ADMIN".equals(actor.getRole())) {
                throw new RuntimeException("Unauthorized: You cannot access or modify this booking");
            }
        }

        // Status validation and state machine transitions
        if ("CANCELLED".equals(newStatus)) {
            if (!isUser && !isProvider) {
                throw new RuntimeException("Only the booking client or provider can cancel this booking");
            }
            if ("COMPLETED".equals(booking.getStatus()) || "REJECTED".equals(booking.getStatus())) {
                throw new RuntimeException("Cannot cancel a completed or rejected booking");
            }
            booking.setStatus("CANCELLED");
        } else if ("ACCEPTED".equals(newStatus) || "REJECTED".equals(newStatus) || "COMPLETED".equals(newStatus)) {
            if (!isProvider) {
                // Admin override allowed
                User actor = userRepository.findById(actorId).orElse(null);
                if (actor == null || !"ADMIN".equals(actor.getRole())) {
                    throw new RuntimeException("Only the service provider can accept/reject/complete this booking");
                }
            }
            booking.setStatus(newStatus);
        } else {
            throw new RuntimeException("Invalid booking status: " + newStatus);
        }

        booking = bookingRepository.save(booking);

        // Notify parties
        sendEmailNotification(booking, newStatus);

        return booking;
    }

    public List<Booking> getBookingsForUser(UUID userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Booking> getBookingsForProvider(UUID providerId) {
        return bookingRepository.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    private void sendEmailNotification(Booking booking, String eventType) {
        if (mailSender == null) {
            System.out.println("[Mail Notice] JavaMailSender is unconfigured. Event: Booking ID " + booking.getId() + " was " + eventType);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@localservicefinder.com");

            String providerEmail = booking.getService().getProvider().getUser().getEmail();
            String customerEmail = booking.getUser().getEmail();

            if ("CREATED".equals(eventType)) {
                message.setTo(providerEmail);
                message.setSubject("New Service Booking Request - Local Service Finder");
                message.setText("Hello,\n\nYou have received a new service booking request.\n\n" +
                        "Customer: " + booking.getUser().getFullName() + "\n" +
                        "Service: " + booking.getService().getTitle() + "\n" +
                        "Date/Time: " + booking.getBookingDate() + "\n" +
                        "Notes: " + booking.getNotes() + "\n\n" +
                        "Please login to your provider dashboard to accept or reject this request.");
            } else {
                message.setTo(customerEmail);
                message.setSubject("Booking Status Update - Local Service Finder");
                message.setText("Hello " + booking.getUser().getFullName() + ",\n\n" +
                        "Your booking (ID: " + booking.getId() + ") for '" + booking.getService().getTitle() + "' has been updated.\n" +
                        "New Status: " + booking.getStatus() + "\n" +
                        "Date/Time: " + booking.getBookingDate() + "\n\n" +
                        "Thank you for using Local Service Finder!");
            }

            mailSender.send(message);
            System.out.println("[Mail Notice] Email sent successfully for Booking ID " + booking.getId());
        } catch (Exception e) {
            // Catch mail errors so database transaction is not rolled back if SMTP config is incorrect.
            System.err.println("Failed to send booking status email notification: " + e.getMessage());
        }
    }
}
