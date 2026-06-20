package com.example.travel_backend.repository;

import com.example.travel_backend.entity.CommentReaction; // Nhớ import đúng Entity của ông
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {

    @Query("SELECT cr.comment.id FROM CommentReaction cr WHERE cr.user.id = :userId AND cr.comment.id IN :commentIds AND cr.reactionType = 'like'")
    List<UUID> findLikedCommentIdsByUser(@Param("userId") UUID userId, @Param("commentIds") List<UUID> commentIds);
    void deleteByUserIdAndCommentId(UUID userId, UUID commentId);

    void deleteByComment_Id(UUID commentId);
}