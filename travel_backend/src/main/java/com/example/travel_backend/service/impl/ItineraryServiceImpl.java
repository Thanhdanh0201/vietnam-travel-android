package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.UpdateItineraryDto;
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

    @Autowired
    private com.example.travel_backend.repository.UserRepository userRepository;

    @Autowired
    private com.example.travel_backend.repository.PlaceRepository placeRepository;

    @Autowired
    private com.example.travel_backend.repository.ItineraryItemRepository itineraryItemRepository;

    @Autowired
    private com.example.travel_backend.repository.ItineraryCollaboratorRepository collaboratorRepository;

    public List<ItineraryResponseDto> getPublicItineraries(UUID userId, int limit, int offset) {
        System.out.println("Fetching public itineraries for user: " + userId);

        return itineraryRepository.findPublicItineraries(userId, PageRequest.of(offset / limit, limit))
                .getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private boolean hasModifyPermission(Itinerary itinerary, UUID requesterId) {
        if (itinerary.getUser().getId().equals(requesterId)) {
            return true;
        }
        return userRepository.findById(requesterId)
                .map(user -> {
                    String email = user.getEmail();
                    if (email == null || email.isBlank()) {
                        return false;
                    }
                    return collaboratorRepository.findByItinerary_IdAndEmail(itinerary.getId(), email.trim())
                            .map(c -> "EDIT".equalsIgnoreCase(c.getRole()))
                            .orElse(false);
                })
                .orElse(false);
    }

    public void updateItineraryStatus(UUID itineraryId, UUID requesterId, UpdateItineraryDto updates) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        if (!hasModifyPermission(itinerary, requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to update this itinerary");
        }

        boolean isModified = false;

        if (updates.getTitle() != null) {
            itinerary.setTitle(updates.getTitle());
            isModified = true;
        }

        if (updates.getDescription() != null) {
            itinerary.setDescription(updates.getDescription());
            isModified = true;
        }

        if (updates.getIsPublic() != null) {
            itinerary.setIsPublic(updates.getIsPublic());
            isModified = true;
        }

        if (isModified) {
            itineraryRepository.save(itinerary);
            System.out.println("Updated itinerary " + itineraryId + " successfully.");
        }
    }

    public ItineraryResponseDto createItinerary(UUID userId, com.example.travel_backend.dto.request.CreateItineraryRequestDto request) {
        com.example.travel_backend.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        Itinerary itinerary = new Itinerary();
        itinerary.setUser(user);
        itinerary.setTitle(request.getTitle());
        itinerary.setLocation(request.getLocation());
        itinerary.setStartDate(request.getStartDate());
        itinerary.setEndDate(request.getEndDate());
        itinerary.setDescription(request.getDescription());
        itinerary.setCoverUrl(request.getCoverUrl());
        itinerary.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        itinerary.setCreatedAt(java.time.OffsetDateTime.now());
        itinerary.setShareCount(0);
        itinerary.setStatus("draft");

        Itinerary saved = itineraryRepository.save(itinerary);
        return mapToDto(saved);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteItinerary(UUID itineraryId, UUID requesterId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        if (!itinerary.getUser().getId().equals(requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to delete this itinerary");
        }

        itineraryItemRepository.deleteByItineraryId(itineraryId);
        collaboratorRepository.deleteByItineraryId(itineraryId);
        itineraryRepository.delete(itinerary);
    }

    public List<ItineraryResponseDto> getMyItineraries(UUID userId) {
        String email = userRepository.findById(userId)
                .map(com.example.travel_backend.entity.User::getEmail)
                .orElse("");
        return itineraryRepository.findMyAndCollaborativeItineraries(userId, email.trim())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<com.example.travel_backend.dto.response.ItineraryItemResponseDto> getItineraryItems(UUID itineraryId) {
        return itineraryItemRepository.findByItineraryIdOrderByScheduledTimeAsc(itineraryId)
                .stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
    }

    public com.example.travel_backend.dto.response.ItineraryItemResponseDto addItineraryItem(UUID itineraryId, UUID requesterId, com.example.travel_backend.dto.request.CreateItineraryItemRequestDto request) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        if (!hasModifyPermission(itinerary, requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to modify this itinerary");
        }

        com.example.travel_backend.entity.Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Place not found"));

        com.example.travel_backend.entity.ItineraryItem item = new com.example.travel_backend.entity.ItineraryItem();
        item.setItinerary(itinerary);
        item.setPlace(place);
        item.setScheduledTime(request.getScheduledTime());
        item.setDay(request.getDay());
        item.setNote(request.getNote());
        item.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);

        com.example.travel_backend.entity.ItineraryItem saved = itineraryItemRepository.save(item);
        return mapItemToDto(saved);
    }

    public void deleteItineraryItem(UUID itineraryId, UUID itemId, UUID requesterId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        if (!hasModifyPermission(itinerary, requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to modify this itinerary");
        }

        com.example.travel_backend.entity.ItineraryItem item = itineraryItemRepository.findById(itemId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary item not found"));

        if (!item.getItinerary().getId().equals(itineraryId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Item does not belong to this itinerary");
        }

        itineraryItemRepository.delete(item);
    }

    private com.example.travel_backend.dto.response.ItineraryItemResponseDto mapItemToDto(com.example.travel_backend.entity.ItineraryItem item) {
        com.example.travel_backend.dto.response.ItineraryItemResponseDto dto = new com.example.travel_backend.dto.response.ItineraryItemResponseDto();
        dto.setId(item.getId());
        dto.setPlaceId(item.getPlace().getId());
        dto.setPlaceName(item.getPlace().getName());
        dto.setImageUrl(item.getPlace().getImageUrl());
        dto.setTag(item.getPlace().getType());
        
        String city = item.getPlace().getCity() != null ? item.getPlace().getCity().getName() : "";
        String province = item.getPlace().getProvince() != null ? item.getPlace().getProvince().getName() : "";
        String location = (city + ", " + province).trim().replaceAll("^,\\s*|\\s*,\\s*$", "");
        dto.setLocation(location);

        dto.setScheduledTime(item.getScheduledTime());
        dto.setDay(item.getDay());
        dto.setNote(item.getNote());
        dto.setOrderIndex(item.getOrderIndex());
        dto.setWarningType(item.getWarningType());
        dto.setWarningValue(item.getWarningValue());
        return dto;
    }

    private ItineraryResponseDto mapToDto(Itinerary i) {
        ItineraryResponseDto dto = new ItineraryResponseDto();
        dto.setId(i.getId());
        dto.setTitle(i.getTitle());
        dto.setLocation(i.getLocation());
        dto.setDescription(i.getDescription());
        dto.setCoverUrl(i.getCoverUrl());
        dto.setStartDate(i.getStartDate());
        dto.setEndDate(i.getEndDate());
        dto.setShareCount(i.getShareCount());
        dto.setCreatedAt(i.getCreatedAt());
        dto.setItemCount(itineraryRepository.countItemsByItineraryId(i.getId()));
        dto.setStatus(i.getStatus());
        return dto;
    }
}