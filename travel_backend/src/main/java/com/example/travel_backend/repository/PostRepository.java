package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
