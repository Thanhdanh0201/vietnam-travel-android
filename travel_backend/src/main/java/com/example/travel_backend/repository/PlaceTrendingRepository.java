package com.example.travel_backend.repository;

import com.example.travel_backend.entity.PlaceTrending;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaceTrendingRepository extends JpaRepository<PlaceTrending, UUID> {
    List<PlaceTrending> findAllByOrderByScoreDesc(Pageable pageable);
}