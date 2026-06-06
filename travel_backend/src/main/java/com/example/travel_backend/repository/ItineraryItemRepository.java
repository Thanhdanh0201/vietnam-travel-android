package com.example.travel_backend.repository;

import com.example.travel_backend.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, UUID> {

    @Query("SELECT item FROM ItineraryItem item WHERE item.itinerary.id = :itineraryId ORDER BY item.orderIndex ASC")
    List<ItineraryItem> findByItineraryIdOrderByOrderIndexAsc(@Param("itineraryId") UUID itineraryId);
    
    @Modifying
    @Query("DELETE FROM ItineraryItem i WHERE i.itinerary.id = :itineraryId")
    void deleteByItineraryId(@Param("itineraryId") UUID itineraryId);

    // Bổ sung hàm đếm số lượng item theo list ID để chống N+1 Query
    @Query("SELECT i.itinerary.id, COUNT(i) FROM ItineraryItem i WHERE i.itinerary.id IN :itineraryIds GROUP BY i.itinerary.id")
    List<Object[]> countItemsByItineraryIds(@Param("itineraryIds") List<UUID> itineraryIds);
}
