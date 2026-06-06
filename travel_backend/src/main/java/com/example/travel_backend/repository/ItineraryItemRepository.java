package com.example.travel_backend.repository;

import com.example.travel_backend.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, UUID> {
    List<ItineraryItem> findByItineraryIdOrderByScheduledTimeAsc(UUID itineraryId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ItineraryItem i WHERE i.itinerary.id = :itineraryId")
    void deleteByItineraryId(@org.springframework.data.repository.query.Param("itineraryId") UUID itineraryId);
}
