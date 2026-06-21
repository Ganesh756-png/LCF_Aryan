package com.localservicefinder.service;

import com.localservicefinder.entity.Feedback;
import com.localservicefinder.entity.ServiceProvider;
import com.localservicefinder.entity.User;
import com.localservicefinder.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceProviderRepository providerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalProviders = providerRepository.count();
        long pendingApprovals = providerRepository.findByIsApproved(false).size();
        long totalBookings = bookingRepository.count();
        long totalCategories = categoryRepository.count();
        long activeComplaints = feedbackRepository.findByStatusOrderByCreatedAtDesc("PENDING").size();

        stats.put("totalUsers", totalUsers);
        stats.put("totalProviders", totalProviders);
        stats.put("pendingApprovals", pendingApprovals);
        stats.put("totalBookings", totalBookings);
        stats.put("totalCategories", totalCategories);
        stats.put("activeComplaints", activeComplaints);

        // Fetch recent users & bookings
        stats.put("recentUsers", userRepository.findAll().stream().limit(5).toList());
        stats.put("recentBookings", bookingRepository.findAll().stream().limit(5).toList());

        return stats;
    }

    @Transactional
    public ServiceProvider approveProvider(UUID providerId) {
        ServiceProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));
        provider.setIsApproved(true);
        return providerRepository.save(provider);
    }

    @Transactional
    public User setUserStatus(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        status = status.toUpperCase();
        if (!"ACTIVE".equals(status) && !"SUSPENDED".equals(status)) {
            throw new RuntimeException("Invalid user status: " + status);
        }
        
        user.setStatus(status);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<ServiceProvider> getAllProviders() {
        return providerRepository.findAll();
    }
}
