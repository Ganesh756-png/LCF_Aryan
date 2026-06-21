package com.localservicefinder.service;

import com.localservicefinder.dto.RegisterRequest;
import com.localservicefinder.entity.Admin;
import com.localservicefinder.entity.User;
import com.localservicefinder.entity.ServiceProvider;
import com.localservicefinder.repository.AdminRepository;
import com.localservicefinder.repository.UserRepository;
import com.localservicefinder.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceProviderRepository providerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        // Generate UUID locally or accept one if synced from Supabase
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole().toUpperCase());
        user.setStatus("ACTIVE");

        user = userRepository.save(user);

        if ("PROVIDER".equals(user.getRole())) {
            ServiceProvider provider = new ServiceProvider(user);
            provider.setBusinessName(request.getBusinessName());
            provider.setBio(request.getBio());
            provider.setAddress(request.getAddress());
            provider.setCity(request.getCity());
            provider.setYearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : 0);
            provider.setWhatsappNumber(request.getWhatsappNumber());
            provider.setIsApproved(false); // default pending approval
            providerRepository.save(provider);
        } else if ("ADMIN".equals(user.getRole())) {
            Admin admin = new Admin(user);
            admin.setIsSuper(false);
            adminRepository.save(admin);
        }

        return user;
    }

    // Supabase account synchronization fallback
    @Transactional
    public User syncSupabaseUser(UUID id, String email, String fullName, String role) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isPresent()) {
            return existing.get();
        }

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(""); // Managed by Supabase
        user.setFullName(fullName != null ? fullName : "Supabase User");
        user.setRole(role != null ? role.toUpperCase() : "USER");
        user.setStatus("ACTIVE");

        user = userRepository.save(user);

        if ("PROVIDER".equals(user.getRole())) {
            ServiceProvider provider = new ServiceProvider(user);
            provider.setIsApproved(false);
            providerRepository.save(provider);
        }

        return user;
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUserProfile(UUID userId, String fullName, String phone, String profileImage) {
        User user = findById(userId);
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (profileImage != null) {
            user.setProfileImage(profileImage);
        }
        return userRepository.save(user);
    }
}
