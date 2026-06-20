package com.example.travel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "itinerary_notes")
public class ItineraryNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    /**
     * Null = ghi chú chung của cả lịch trình (hiển thị ở cuối timeline).
     * Non-null = ghi chú nhóm gắn với một địa điểm cụ thể.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "itinerary_item_id")
    private ItineraryItem itineraryItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @Column(name = "image_url", length = Integer.MAX_VALUE)
    private String imageUrl;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
