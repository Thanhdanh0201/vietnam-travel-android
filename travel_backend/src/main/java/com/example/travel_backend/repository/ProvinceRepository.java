package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    Optional<Province> findByCode(String code);
}