package com.localservicefinder.repository;

import com.localservicefinder.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT b FROM Booking b WHERE b.service.provider.id = :providerId ORDER BY b.createdAt DESC")
    List<Booking> findByProviderIdOrderByCreatedAtDesc(@Param("providerId") UUID providerId);
}
