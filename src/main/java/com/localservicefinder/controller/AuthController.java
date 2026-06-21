package com.localservicefinder.controller;

import com.localservicefinder.config.JwtTokenProvider;
import com.localservicefinder.dto.AuthRequest;
import com.localservicefinder.dto.AuthResponse;
import com.localservicefinder.dto.RegisterRequest;
import com.localservicefinder.entity.ServiceProvider;
import com.localservicefinder.entity.User;
import com.localservicefinder.repository.ServiceProviderRepository;
import com.localservicefinder.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ServiceProviderRepository providerRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User registered = userService.registerUser(request);
            return ResponseEntity.ok(registered);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest request) {
        try {
            Optional<User> userOpt = userService.findByEmail(request.getEmail());
            if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid email or password");
                return ResponseEntity.status(401).body(response);
            }

            User user = userOpt.get();
            if ("SUSPENDED".equals(user.getStatus())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Your account has been suspended by administrators.");
                return ResponseEntity.status(403).body(response);
            }

            String jwt = tokenProvider.generateToken(user);
            Boolean isApproved = null;

            if ("PROVIDER".equals(user.getRole())) {
                Optional<ServiceProvider> providerOpt = providerRepository.findById(user.getId());
                if (providerOpt.isPresent()) {
                    isApproved = providerOpt.get().getIsApproved();
                }
            }

            return ResponseEntity.ok(new AuthResponse(jwt, user.getId(), user.getEmail(), user.getFullName(), user.getRole(), isApproved));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Client can hit /me with JWT to verify session and retrieve their user context
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                User user = (User) principal;
                Boolean isApproved = null;
                if ("PROVIDER".equals(user.getRole())) {
                    Optional<ServiceProvider> providerOpt = providerRepository.findById(user.getId());
                    if (providerOpt.isPresent()) {
                        isApproved = providerOpt.get().getIsApproved();
                    }
                }
                
                // Return fresh status
                return ResponseEntity.ok(new AuthResponse(
                        null, // no need to re-issue token here
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole(),
                        isApproved
                ));
            }
            return ResponseEntity.status(401).body("Unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }
}
