package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {
    List<Place> findByProvince_Code(String code, Pageable pageable);
    List<Place> findByProvince_CodeAndType(String code, String type, Pageable pageable);

    /**
     * Điểm gần tọa độ GPS nhất (Haversine, mét) — dùng cho thời tiết theo vị trí thực.
     */
    @Query(value = """
            SELECT p.* FROM places p
            ORDER BY (
                6371000 * acos(LEAST(1.0, GREATEST(-1.0,
                    cos(radians(:lat)) * cos(radians(p.lat)) * cos(radians(p.lng) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(p.lat))
                )))
            ) ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Place> findNearestByCoordinates(@Param("lat") double lat, @Param("lng") double lng);
}