package com.example.travel_backend.repository;

import com.example.travel_backend.entity.ItineraryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryNoteRepository extends JpaRepository<ItineraryNote, UUID> {

    /**
     * Lấy ghi chú nhóm của một địa điểm cụ thể (có item_id).
     */
    List<ItineraryNote> findByItinerary_IdAndItineraryItem_IdOrderByCreatedAtAsc(
            UUID itineraryId, UUID itineraryItemId);

    /**
     * Lấy ghi chú chung của cả lịch trình (item_id = null).
     */
    List<ItineraryNote> findByItinerary_IdAndItineraryItemIsNullOrderByCreatedAtAsc(UUID itineraryId);

    /**
     * Lấy TẤT CẢ ghi chú của lịch trình (cả có item và không có item).
     */
    @Query("SELECT n FROM ItineraryNote n WHERE n.itinerary.id = :itineraryId ORDER BY n.createdAt ASC")
    List<ItineraryNote> findAllByItineraryIdOrderByCreatedAtAsc(@Param("itineraryId") UUID itineraryId);

    void deleteByItinerary_Id(UUID itineraryId);
}
