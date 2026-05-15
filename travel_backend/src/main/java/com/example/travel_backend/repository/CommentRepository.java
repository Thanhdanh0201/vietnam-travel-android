package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(UUID postId, Pageable pageable);
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
}