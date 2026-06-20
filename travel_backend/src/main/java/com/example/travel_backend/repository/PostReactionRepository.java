package com.example.travel_backend.repository;

import com.example.travel_backend.entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostReactionRepository extends JpaRepository<PostReaction, UUID> {
    @Query("SELECT pr.post.id FROM PostReaction pr WHERE pr.user.id = :userId AND pr.post.id IN :postIds AND pr.reactionType = 'like'")
    List<UUID> findLikedPostIdsByUser(@Param("userId") UUID userId, @Param("postIds") List<UUID> postIds);

    void deleteByUserIdAndPostId(UUID userId, UUID postId);

    void deleteByPost_Id(UUID postId);
}
