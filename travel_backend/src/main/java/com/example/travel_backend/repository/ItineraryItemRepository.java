package com.example.travel_backend.repository;

import com.example.travel_backend.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, UUID> {
    List<ItineraryItem> findByItineraryIdOrderByScheduledTimeAsc(UUID itineraryId);
}
