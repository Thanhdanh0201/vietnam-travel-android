package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.EventDto;
import com.example.travel_backend.entity.Event;
import com.example.travel_backend.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Trang chủ: lễ hội sắp tới (giao cửa sổ 3 tháng mặc định). Phải khai báo trước /{id} để không match "upcoming".
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDto>> getUpcoming(
            @RequestParam(name = "months", defaultValue = "3") int months,
            @RequestParam(name = "limit", defaultValue = "30") int limit
    ) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(months, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable UUID id) {
        Event event = eventService.getEventById(id);
        if (event != null) {
            return ResponseEntity.ok(event);
        }
        return ResponseEntity.notFound().build();
    }
}