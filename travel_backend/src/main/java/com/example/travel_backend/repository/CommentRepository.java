package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(
            @Param("postId") UUID postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId AND (c.isDeleted = false OR c.isDeleted IS NULL) ORDER BY c.createdAt ASC")
    List<Comment> findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("parentCommentId") UUID parentCommentId);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    List<Comment> findByPost_IdAndIsDeletedFalse(@Param("postId") UUID postId);

    @Query("SELECT c FROM Comment c WHERE c.id = :id AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    Optional<Comment> findByIdAndIsDeletedFalse(@Param("id") UUID id);
}
