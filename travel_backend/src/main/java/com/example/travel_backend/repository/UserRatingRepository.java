package com.example.travel_backend.repository;

import com.example.travel_backend.entity.UserRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRatingRepository extends JpaRepository<UserRating, UUID> {

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            SELECT ur FROM UserRating ur
            WHERE ur.place.id = :placeId
            ORDER BY ur.createdAt DESC NULLS LAST
            """)
    List<UserRating> findByPlace_IdOrderByCreatedAtDesc(
            @Param("placeId") UUID placeId,
            Pageable pageable);
}
