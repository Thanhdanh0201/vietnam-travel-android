package com.example.travel_backend.mapper;

import com.example.travel_backend.dto.response.EventDto;
import com.example.travel_backend.entity.Event;

public final class EventMapper {

    private EventMapper() {
    }

    public static EventDto toDto(Event e) {
        EventDto dto = new EventDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setType(e.getType());
        dto.setImage_url(e.getImageUrl());
        dto.setStart_date(e.getStartDate() != null ? e.getStartDate().toString() : null);
        dto.setEnd_date(e.getEndDate() != null ? e.getEndDate().toString() : null);
        if (e.getProvince() != null) {
            dto.setProvince_name(e.getProvince().getName());
        }
        if (e.getPlace() != null) {
            dto.setPlace_name(e.getPlace().getName());
        }
        return dto;
    }
}
