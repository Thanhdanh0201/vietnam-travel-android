package com.example.travel_backend.repository;

import com.example.travel_backend.entity.SearchKeywordLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SearchKeywordLogRepository extends JpaRepository<SearchKeywordLog, UUID> {

    @Query("""
            SELECT s.query
            FROM SearchKeywordLog s
            WHERE s.createdAt >= :since
            GROUP BY s.query
            ORDER BY COUNT(s) DESC
            LIMIT :limit
            """)
    List<String> findTopKeywords(
            @Param("since") OffsetDateTime since,
            @Param("limit") int limit
    );
}
