package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.ItineraryResponseDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ItineraryService {
    // API 3.4: Lấy danh sách lịch trình công khai của user
    List<ItineraryResponseDto> getPublicItineraries(UUID userId, int limit, int offset);

    // API 3.4: Toggle trạng thái công khai/riêng tư
    void updateItineraryVisibility(UUID itineraryId, UUID ownerId, Map<String, Object> updates);
}