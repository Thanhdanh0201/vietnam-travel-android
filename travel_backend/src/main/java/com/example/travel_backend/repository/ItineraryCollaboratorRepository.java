package com.example.travel_backend.repository;

import com.example.travel_backend.entity.ItineraryCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ItineraryCollaboratorRepository extends JpaRepository<ItineraryCollaborator, UUID> {
    List<ItineraryCollaborator> findByItinerary_Id(UUID itineraryId);
    java.util.Optional<ItineraryCollaborator> findByItinerary_IdAndEmail(UUID itineraryId, String email);
    void deleteByItinerary_IdAndEmail(UUID itineraryId, String email);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ItineraryCollaborator c WHERE c.itinerary.id = :itineraryId")
    void deleteByItineraryId(@org.springframework.data.repository.query.Param("itineraryId") UUID itineraryId);
}
