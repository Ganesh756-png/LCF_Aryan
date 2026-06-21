package com.localservicefinder.repository;

import com.localservicefinder.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, UUID> {
    List<ServiceProvider> findByIsApproved(Boolean isApproved);
    List<ServiceProvider> findByCityIgnoreCase(String city);
}
