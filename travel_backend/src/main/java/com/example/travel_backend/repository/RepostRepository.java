package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Repost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepostRepository extends JpaRepository<Repost, UUID> {

    void deleteByUserIdAndPostId(UUID userId, UUID postId);
}