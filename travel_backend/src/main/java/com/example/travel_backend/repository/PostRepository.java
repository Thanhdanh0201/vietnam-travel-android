package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Post> findByVisibilityAndIsDeletedFalseOrderByCreatedAtDesc(String visibility, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.user.id IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) ORDER BY p.createdAt DESC")
    Page<Post> findFollowingPosts(@Param("userId") UUID userId, Pageable pageable);

    List<Post> findByOriginalPost_IdAndIsDeletedFalse(UUID originalPostId);

    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.originalPost.id = :originalPostId AND p.isDeleted = false")
    Optional<Post> findActiveRepostPost(@Param("userId") UUID userId, @Param("originalPostId") UUID originalPostId);

    Optional<Post> findByIdAndIsDeletedFalse(UUID id);
}
