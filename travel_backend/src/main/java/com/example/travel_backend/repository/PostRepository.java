package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Dung cho API: /api/posts/public (4.1 Feed cong khai)
    Page<Post> findByVisibilityOrderByCreatedAtDesc(String visibility, Pageable pageable);

    // Dung cho API: /api/posts/following (4.1 Feed following only)
    @Query("SELECT p FROM Post p WHERE p.user.id IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) ORDER BY p.createdAt DESC")
    Page<Post> findFollowingPosts(@Param("userId") UUID userId, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM Post p WHERE p.user.id = :userId AND p.originalPost.id = :originalPostId")
    void deleteRepostPost(@Param("userId") UUID userId, @Param("originalPostId") UUID originalPostId);

    List<Post> findByOriginalPost_Id(UUID originalPostId);
}
