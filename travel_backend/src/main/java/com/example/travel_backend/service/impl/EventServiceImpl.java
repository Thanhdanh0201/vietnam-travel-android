package com.example.travel_backend.service.impl;

import com.example.travel_backend.entity.Event;
import com.example.travel_backend.repository.EventRepository;
import com.example.travel_backend.service.EventService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event getEventById(UUID id) {
        System.out.println("Get event by id: " + id);
        return eventRepository.findById(id).orElse(null);
    }
}