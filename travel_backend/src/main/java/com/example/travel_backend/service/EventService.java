package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.EventDto;
import com.example.travel_backend.entity.Event;

import java.util.List;
import java.util.UUID;

public interface EventService {
    Event getEventById(UUID id);

    List<EventDto> getUpcomingEvents(int months, int limit);
    List<EventDto> getUpcomingEventsPaged(int months, int limit, int offset);
    List<EventDto> getAllEventsPaged(int limit, int offset);
}