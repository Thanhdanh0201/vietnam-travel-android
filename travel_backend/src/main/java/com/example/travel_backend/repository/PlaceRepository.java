package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Place;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {
    List<Place> findByProvince_Code(String code, Pageable pageable);
    List<Place> findByProvince_CodeAndType(String code, String type, Pageable pageable);
}