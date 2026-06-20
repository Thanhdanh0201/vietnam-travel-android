package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Mention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MentionRepository extends JpaRepository<Mention, UUID> {
    void deleteByPost_Id(UUID postId);
}
