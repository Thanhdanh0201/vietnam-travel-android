package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByProvince_Code(String code);
    List<Event> findByPlace_Id(UUID placeId);
}