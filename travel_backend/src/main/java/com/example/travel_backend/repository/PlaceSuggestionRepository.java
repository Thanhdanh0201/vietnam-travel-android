package com.example.travel_backend.repository;

import com.example.travel_backend.entity.PlaceSuggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaceSuggestionRepository extends JpaRepository<PlaceSuggestion, UUID> {
    Page<PlaceSuggestion> findByStatus(String status, Pageable pageable);
    Page<PlaceSuggestion> findByUser_Id(UUID userId, Pageable pageable);
}
