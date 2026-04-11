package com.example.travel_backend.service;

import com.example.travel_backend.entity.Event;
import java.util.UUID;

public interface EventService {
    Event getEventById(UUID id);
}