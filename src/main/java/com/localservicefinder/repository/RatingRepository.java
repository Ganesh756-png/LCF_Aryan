package com.localservicefinder.repository;

import com.localservicefinder.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByProviderIdOrderByCreatedAtDesc(UUID providerId);

    Optional<Rating> findByBookingId(Long bookingId);

    @Query("SELECT COALESCE(AVG(r.ratingValue), 0.0) FROM Rating r WHERE r.provider.id = :providerId")
    Double findAverageRatingByProviderId(@Param("providerId") UUID providerId);
}
