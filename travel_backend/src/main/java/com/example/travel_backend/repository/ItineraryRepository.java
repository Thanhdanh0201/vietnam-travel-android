package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Itinerary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    @Query("SELECT i FROM Itinerary i WHERE i.user.id = :userId AND i.isPublic = true ORDER BY i.createdAt DESC")
    Page<Itinerary> findPublicItineraries(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(item) FROM ItineraryItem item WHERE item.itinerary.id = :itineraryId")
    Integer countItemsByItineraryId(@Param("itineraryId") UUID itineraryId);

    java.util.List<Itinerary> findByUserIdOrderByCreatedAtDesc(UUID userId);
}