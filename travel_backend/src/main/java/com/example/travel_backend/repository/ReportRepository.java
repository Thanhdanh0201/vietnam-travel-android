package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findByStatus(String status, Pageable pageable);

    List<Report> findByReportedPost_Id(UUID postId);

    List<Report> findByReportedComment_Id(UUID commentId);

    @Modifying
    @Query("UPDATE Report r SET r.reportedPost = null WHERE r.reportedPost.id = :postId")
    void clearReportedPostReferences(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Report r SET r.reportedComment = null WHERE r.reportedComment.id = :commentId")
    void clearReportedCommentReferences(@Param("commentId") UUID commentId);
}