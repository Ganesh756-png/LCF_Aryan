package com.localservicefinder.controller;

import com.localservicefinder.entity.*;
import com.localservicefinder.service.AdminService;
import com.localservicefinder.service.RatingFeedbackService;
import com.localservicefinder.service.ServiceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServiceCategoryService categoryService;

    @Autowired
    private RatingFeedbackService ratingFeedbackService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/providers")
    public ResponseEntity<List<ServiceProvider>> getAllProviders() {
        return ResponseEntity.ok(adminService.getAllProviders());
    }

    @PutMapping("/providers/{id}/approve")
    public ResponseEntity<?> approveProvider(@PathVariable("id") UUID id) {
        try {
            ServiceProvider approved = adminService.approveProvider(id);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> setUserStatus(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            User updated = adminService.setUserStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<?> saveCategory(@RequestBody ServiceCategory category) {
        try {
            ServiceCategory saved = categoryService.saveCategory(category);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        try {
            categoryService.deleteCategory(id);
            Map<String, String> msg = new HashMap<>();
            msg.put("message", "Category deleted successfully");
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<Feedback>> getAllFeedbackTickets() {
        return ResponseEntity.ok(ratingFeedbackService.getAllFeedback());
    }

    @PutMapping("/feedback/{id}/resolve")
    public ResponseEntity<?> resolveFeedback(@PathVariable("id") Long id) {
        try {
            Feedback resolved = ratingFeedbackService.resolveFeedback(id);
            return ResponseEntity.ok(resolved);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
