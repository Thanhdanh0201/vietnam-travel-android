package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.EventDto;
import com.example.travel_backend.entity.Event;
import com.example.travel_backend.mapper.EventMapper;
import com.example.travel_backend.repository.EventRepository;
import com.example.travel_backend.service.EventService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event getEventById(UUID id) {
        System.out.println("Get event by id: " + id);
        return eventRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getUpcomingEvents(int months, int limit) {
        return getUpcomingEventsPaged(months, limit, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getUpcomingEventsPaged(int months, int limit, int offset) {
        int m = Math.min(Math.max(months, 1), 12);
        int lim = Math.min(Math.max(limit, 1), 100);
        int page = lim > 0 ? offset / lim : 0;
        LocalDate today = LocalDate.now(VN);
        LocalDate windowEnd = today.plusMonths(m);
        return eventRepository.findUpcomingInWindow(
                today,
                windowEnd,
                PageRequest.of(page, lim, Sort.by(Sort.Direction.ASC, "startDate"))
        )
                .stream()
                .map(EventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getAllEventsPaged(int limit, int offset) {
        int lim = Math.min(Math.max(limit, 1), 100);
        int page = lim > 0 ? offset / lim : 0;
        return eventRepository.findAllByOrderByStartDateAsc(
                PageRequest.of(page, lim, Sort.by(Sort.Direction.ASC, "startDate"))
        )
                .stream()
                .map(EventMapper::toDto)
                .collect(Collectors.toList());
    }
}