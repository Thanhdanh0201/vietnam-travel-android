package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = {"province", "place"})
    List<Event> findByProvince_Code(String code);

    @EntityGraph(attributePaths = {"province", "place"})
    List<Event> findByPlace_Id(UUID placeId);

    /**
     * Lễ hội còn diễn ra / sắp tới: giao với [today, windowEnd] (schema: start_date, end_date).
     */
    @EntityGraph(attributePaths = {"province", "place"})
    @Query("""
            SELECT e FROM Event e
            WHERE e.endDate >= :today
              AND e.startDate <= :windowEnd
            ORDER BY e.startDate ASC
            """)
    List<Event> findUpcomingInWindow(
            @Param("today") LocalDate today,
            @Param("windowEnd") LocalDate windowEnd,
            Pageable pageable
    );

    /** Toàn bộ lễ hội, sắp xếp theo ngày bắt đầu. */
    @EntityGraph(attributePaths = {"province", "place"})
    @Query("SELECT e FROM Event e ORDER BY e.startDate ASC")
    List<Event> findAllOrderedByStartDate(Pageable pageable);
}