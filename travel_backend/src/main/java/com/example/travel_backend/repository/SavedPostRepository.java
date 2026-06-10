package com.example.travel_backend.repository;

import com.example.travel_backend.entity.SavedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedPostRepository extends JpaRepository<SavedPost, UUID> {
    Optional<SavedPost> findByUserIdAndPostId(UUID userId, UUID postId);
    
    Page<SavedPost> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT sp.post.id FROM SavedPost sp WHERE sp.user.id = :userId AND sp.post.id IN :postIds")
    List<UUID> findSavedPostIdsByUser(@Param("userId") UUID userId, @Param("postIds") List<UUID> postIds);

    void deleteByUserIdAndPostId(UUID userId, UUID postId);
}
