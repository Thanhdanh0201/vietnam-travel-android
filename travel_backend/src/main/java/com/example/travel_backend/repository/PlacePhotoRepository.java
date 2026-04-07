package com.example.travel_backend.repository;

import com.example.travel_backend.entity.PlacePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlacePhotoRepository extends JpaRepository<PlacePhoto, UUID> {
    List<PlacePhoto> findByPlace_Id(UUID placeId);
}