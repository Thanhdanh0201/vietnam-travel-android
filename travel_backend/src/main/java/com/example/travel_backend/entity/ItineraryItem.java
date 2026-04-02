package com.example.travel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "itinerary_items")
public class ItineraryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "note", length = Integer.MAX_VALUE)
    private String note;

    @Column(name = "warning_type", length = Integer.MAX_VALUE)
    private String warningType;

    @Column(name = "warning_value")
    private Float warningValue;


}