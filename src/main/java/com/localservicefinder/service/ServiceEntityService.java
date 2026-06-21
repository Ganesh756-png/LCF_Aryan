package com.localservicefinder.service;

import com.localservicefinder.dto.ServiceRequest;
import com.localservicefinder.entity.Service;
import com.localservicefinder.entity.ServiceCategory;
import com.localservicefinder.entity.ServiceProvider;
import com.localservicefinder.repository.ServiceCategoryRepository;
import com.localservicefinder.repository.ServiceProviderRepository;
import com.localservicefinder.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceEntityService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceProviderRepository providerRepository;

    @Autowired
    private ServiceCategoryRepository categoryRepository;

    public Service getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service listing not found"));
    }

    @Transactional
    public Service createService(UUID providerId, ServiceRequest request) {
        ServiceProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found. Complete your profile before adding services."));

        if (!provider.getIsApproved()) {
            throw new RuntimeException("Your provider account is pending approval by administrator. You cannot add services yet.");
        }

        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Service service = new Service();
        service.setProvider(provider);
        service.setCategory(category);
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setPriceRate(request.getPriceRate());
        service.setDuration(request.getDuration());
        service.setIsPremium(request.getIsPremium() != null ? request.getIsPremium() : false);

        return serviceRepository.save(service);
    }

    @Transactional
    public Service updateService(Long serviceId, UUID providerId, ServiceRequest request) {
        Service service = getServiceById(serviceId);

        // Ensure provider owns the service listing
        if (!service.getProvider().getId().equals(providerId)) {
            throw new RuntimeException("Unauthorized: You do not own this service listing");
        }

        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        service.setCategory(category);
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setPriceRate(request.getPriceRate());
        service.setDuration(request.getDuration());
        service.setIsPremium(request.getIsPremium() != null ? request.getIsPremium() : false);

        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteService(Long serviceId, UUID providerId) {
        Service service = getServiceById(serviceId);

        if (!service.getProvider().getId().equals(providerId)) {
            throw new RuntimeException("Unauthorized: You do not own this service listing");
        }

        serviceRepository.delete(service);
    }

    public List<Service> getServicesByProvider(UUID providerId) {
        return serviceRepository.findByProviderId(providerId);
    }

    public List<Service> getServicesByCategory(Long categoryId) {
        return serviceRepository.findByCategoryId(categoryId);
    }

    public List<Service> searchServices(String query, Long categoryId, String city) {
        List<Service> results;

        if (categoryId != null && city != null && !city.trim().isEmpty()) {
            results = serviceRepository.findByCategoryAndCity(categoryId, city.trim());
        } else if (categoryId != null) {
            results = serviceRepository.findByCategoryId(categoryId);
        } else if (city != null && !city.trim().isEmpty()) {
            results = serviceRepository.findByCity(city.trim());
        } else {
            results = serviceRepository.findAll();
        }

        // Apply text query filter if provided
        if (query != null && !query.trim().isEmpty()) {
            String lowerQuery = query.toLowerCase().trim();
            results = results.stream()
                    .filter(s -> s.getTitle().toLowerCase().contains(lowerQuery) || 
                                 s.getDescription().toLowerCase().contains(lowerQuery) || 
                                 s.getProvider().getUser().getFullName().toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList());
        }

        // Only return services from approved providers
        return results.stream()
                .filter(s -> s.getProvider().getIsApproved())
                .collect(Collectors.toList());
    }
}
