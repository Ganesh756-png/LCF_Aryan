package com.localservicefinder.repository;

import com.localservicefinder.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    List<Service> findByCategoryId(Long categoryId);
    
    List<Service> findByProviderId(UUID providerId);

    @Query("SELECT s FROM Service s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Service> searchServices(@Param("query") String query);

    @Query("SELECT s FROM Service s JOIN s.provider p WHERE s.category.id = :categoryId AND LOWER(p.city) = LOWER(:city) AND p.isApproved = true")
    List<Service> findByCategoryAndCity(@Param("categoryId") Long categoryId, @Param("city") String city);

    @Query("SELECT s FROM Service s JOIN s.provider p WHERE LOWER(p.city) = LOWER(:city) AND p.isApproved = true")
    List<Service> findByCity(@Param("city") String city);
}
