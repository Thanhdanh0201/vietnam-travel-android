package com.example.travel_backend.repository;

import com.example.travel_backend.entity.ItineraryCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ItineraryCollaboratorRepository extends JpaRepository<ItineraryCollaborator, UUID> {
    List<ItineraryCollaborator> findByItinerary_Id(UUID itineraryId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM ItineraryCollaborator c WHERE c.itinerary.id = :itineraryId AND LOWER(c.email) = LOWER(:email)")
    java.util.Optional<ItineraryCollaborator> findByItinerary_IdAndEmail(
            @org.springframework.data.repository.query.Param("itineraryId") UUID itineraryId,
            @org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ItineraryCollaborator c WHERE c.itinerary.id = :itineraryId AND LOWER(c.email) = LOWER(:email)")
    void deleteByItinerary_IdAndEmail(
            @org.springframework.data.repository.query.Param("itineraryId") UUID itineraryId,
            @org.springframework.data.repository.query.Param("email") String email);


    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ItineraryCollaborator c WHERE c.itinerary.id = :itineraryId")
    void deleteByItineraryId(@org.springframework.data.repository.query.Param("itineraryId") UUID itineraryId);

    @Query("SELECT c.itinerary.id, c.role FROM ItineraryCollaborator c WHERE c.email = :email AND c.itinerary.id IN :itineraryIds")
    List<Object[]> findRolesByItineraryIdsAndEmail(@Param("itineraryIds") List<UUID> itineraryIds, @Param("email") String email);
}
