package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.ItineraryResponseDto;
import com.example.travel_backend.entity.Itinerary;
import com.example.travel_backend.repository.ItineraryRepository;
import com.example.travel_backend.service.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItineraryServiceImpl {

    @Autowired
    private ItineraryRepository itineraryRepository;

    public List<ItineraryResponseDto> getPublicItineraries(UUID userId, int limit, int offset) {
        System.out.println("Fetching public itineraries for user: " + userId);

        return itineraryRepository.findPublicItineraries(userId, PageRequest.of(offset / limit, limit))
                .getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void updateItineraryStatus(UUID itineraryId, UUID requesterId, Map<String, Object> updates) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));

        // Chi owner moi duoc phep thay doi trang thai
        if (!itinerary.getUser().getId().equals(requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to update this itinerary");
        }

        if (updates.containsKey("is_public")) {
            itinerary.setIsPublic((Boolean) updates.get("is_public"));
            itineraryRepository.save(itinerary);
            System.out.println("Updated itinerary " + itineraryId + " isPublic to: " + updates.get("is_public"));
        }
    }

    private ItineraryResponseDto mapToDto(Itinerary i) {
        ItineraryResponseDto dto = new ItineraryResponseDto();
        dto.setId(i.getId());
        dto.setTitle(i.getTitle());
        dto.setDescription(i.getDescription());
        dto.setStartDate(i.getStartDate());
        dto.setEndDate(i.getEndDate());
        dto.setShareCount(i.getShareCount());
        dto.setCreatedAt(i.getCreatedAt());
        dto.setItemCount(itineraryRepository.countItemsByItineraryId(i.getId()));
        return dto;
    }
}