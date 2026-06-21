package com.localservicefinder.service;

import com.localservicefinder.dto.ProviderProfileRequest;
import com.localservicefinder.entity.ServiceProvider;
import com.localservicefinder.entity.User;
import com.localservicefinder.repository.ServiceProviderRepository;
import com.localservicefinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceProviderService {

    @Autowired
    private ServiceProviderRepository providerRepository;

    @Autowired
    private UserRepository userRepository;

    public ServiceProvider getProfile(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));
    }

    @Transactional
    public ServiceProvider updateProfile(UUID id, ProviderProfileRequest request) {
        ServiceProvider provider = getProfile(id);
        User user = provider.getUser();

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);

        if (request.getBusinessName() != null) {
            provider.setBusinessName(request.getBusinessName());
        }
        if (request.getBio() != null) {
            provider.setBio(request.getBio());
        }
        if (request.getAddress() != null) {
            provider.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            provider.setCity(request.getCity());
        }
        if (request.getYearsOfExperience() != null) {
            provider.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getWhatsappNumber() != null) {
            provider.setWhatsappNumber(request.getWhatsappNumber());
        }

        return providerRepository.save(provider);
    }

    public List<ServiceProvider> getApprovedProviders() {
        return providerRepository.findByIsApproved(true);
    }

    public List<ServiceProvider> getPendingProviders() {
        return providerRepository.findByIsApproved(false);
    }

    public List<ServiceProvider> getProvidersByCity(String city) {
        return providerRepository.findByCityIgnoreCase(city);
    }
}
