package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findByStatus(String status, Pageable pageable);

    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.reportedPost WHERE r.id = :id")
    Optional<Report> findByIdWithReportedPost(@Param("id") UUID id);

    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.reportedComment WHERE r.id = :id")
    Optional<Report> findByIdWithReportedComment(@Param("id") UUID id);

    List<Report> findByReportedPost_Id(UUID postId);

    List<Report> findByReportedComment_Id(UUID commentId);

    List<Report> findByReportedPost_IdAndStatus(UUID postId, String status);

    List<Report> findByReportedComment_IdAndStatus(UUID commentId, String status);

    List<Report> findByReportedUser_IdAndStatus(UUID userId, String status);

    @Modifying
    @Query("UPDATE Report r SET r.status = :status, r.reviewedAt = :reviewedAt WHERE r.reportedPost.id = :postId")
    void resolveAllForPost(
            @Param("postId") UUID postId,
            @Param("status") String status,
            @Param("reviewedAt") OffsetDateTime reviewedAt);

    @Modifying
    @Query("UPDATE Report r SET r.status = :status, r.reviewedAt = :reviewedAt WHERE r.reportedComment.id = :commentId")
    void resolveAllForComment(
            @Param("commentId") UUID commentId,
            @Param("status") String status,
            @Param("reviewedAt") OffsetDateTime reviewedAt);
}