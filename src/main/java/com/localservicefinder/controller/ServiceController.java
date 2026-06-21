package com.localservicefinder.controller;

import com.localservicefinder.dto.ServiceRequest;
import com.localservicefinder.entity.Service;
import com.localservicefinder.entity.User;
import com.localservicefinder.service.ServiceEntityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ServiceController {

    @Autowired
    private ServiceEntityService serviceEntityService;

    private User getCurrentAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Provider creates a service
    @PostMapping("/api/provider/services")
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceRequest request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Service created = serviceEntityService.createService(currentUser.getId(), request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Provider updates a service listing
    @PutMapping("/api/provider/services/{id}")
    public ResponseEntity<?> updateService(
            @PathVariable("id") Long serviceId,
            @Valid @RequestBody ServiceRequest request) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            Service updated = serviceEntityService.updateService(serviceId, currentUser.getId(), request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Provider deletes a service
    @DeleteMapping("/api/provider/services/{id}")
    public ResponseEntity<?> deleteService(@PathVariable("id") Long serviceId) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            serviceEntityService.deleteService(serviceId, currentUser.getId());
            Map<String, String> msg = new HashMap<>();
            msg.put("message", "Service deleted successfully");
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Public list of services by a specific provider
    @GetMapping("/api/services/provider/{id}")
    public ResponseEntity<List<Service>> getServicesByProvider(@PathVariable("id") UUID providerId) {
        return ResponseEntity.ok(serviceEntityService.getServicesByProvider(providerId));
    }

    // Public list of services in a specific category
    @GetMapping("/api/services/category/{id}")
    public ResponseEntity<List<Service>> getServicesByCategory(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(serviceEntityService.getServicesByCategory(categoryId));
    }

    // Public details of a specific service listing
    @GetMapping("/api/services/detail/{id}")
    public ResponseEntity<?> getServiceDetail(@PathVariable("id") Long serviceId) {
        try {
            return ResponseEntity.ok(serviceEntityService.getServiceById(serviceId));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Public multi-criteria search
    @GetMapping("/api/services/search")
    public ResponseEntity<List<Service>> searchServices(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "city", required = false) String city) {
        List<Service> services = serviceEntityService.searchServices(query, categoryId, city);
        return ResponseEntity.ok(services);
    }
}
