package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.UpdateItineraryDto;
import com.example.travel_backend.dto.response.ItineraryResponseDto;
import com.example.travel_backend.entity.Itinerary;
import com.example.travel_backend.repository.ItineraryRepository;
import com.example.travel_backend.service.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ItineraryResponseDto> getPublicItineraries(UUID userId, int limit, int offset) {
        System.out.println("Fetching public itineraries for user: " + userId);

        List<Itinerary> itineraries = itineraryRepository.findPublicItineraries(userId, PageRequest.of(offset / limit, limit)).getContent();

        if (itineraries.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> itineraryIds = itineraries.stream().map(Itinerary::getId).collect(Collectors.toList());

        // BULK FETCH: Gom nhóm query count item
        Map<UUID, Long> itemCountMap = new HashMap<>();
        for (Object[] result : itineraryItemRepository.countItemsByItineraryIds(itineraryIds)) {
            itemCountMap.put((UUID) result[0], ((Number) result[1]).longValue());
        }

        return itineraries.stream()
                // Do là public feed, role tự mặc định là VIEW
                .map(i -> mapToDto(i, userId, itemCountMap.getOrDefault(i.getId(), 0L), "VIEW"))
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

        // UPDATE LOGIC: Chi owner moi duoc phep thay doi trang thai
        if (!itinerary.getUser().getId().equals(requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Chi owner moi duoc phep thay doi trang thai");
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

        if (updates.getStatus() != null) {
            itinerary.setStatus(updates.getStatus());
            isModified = true;
        }

        if (updates.getCoverUrl() != null) {
            itinerary.setCoverUrl(updates.getCoverUrl());
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
        // Map DTO với itemCount = 0 và Role = OWNER
        return mapToDto(saved, userId, 0L, "OWNER");
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ItineraryResponseDto> getMyItineraries(UUID userId) {
        String email = userRepository.findById(userId)
                .map(com.example.travel_backend.entity.User::getEmail)
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");

        List<Itinerary> itineraries = itineraryRepository.findMyAndCollaborativeItineraries(userId, email);

        if (itineraries.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> itineraryIds = itineraries.stream().map(Itinerary::getId).collect(Collectors.toList());

        // BULK FETCH: 1 Query đếm toàn bộ item của các Itinerary
        Map<UUID, Long> itemCountMap = new HashMap<>();
        for (Object[] result : itineraryItemRepository.countItemsByItineraryIds(itineraryIds)) {
            itemCountMap.put((UUID) result[0], ((Number) result[1]).longValue());
        }

        // BULK FETCH: 1 Query lấy quyền (Role) trên tất cả các Itinerary được share
        Map<UUID, String> roleMap = new HashMap<>();
        if (!email.isEmpty()) {
            for (Object[] result : collaboratorRepository.findRolesByItineraryIdsAndEmail(itineraryIds, email)) {
                roleMap.put((UUID) result[0], (String) result[1]);
            }
        }

        // MAP PURE FUNCTION: Không còn Query ngầm nào phát sinh trong vòng lặp này nữa
        return itineraries.stream()
                .map(i -> mapToDto(
                        i, 
                        userId, 
                        itemCountMap.getOrDefault(i.getId(), 0L),
                        roleMap.getOrDefault(i.getId(), "VIEW")
                ))
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<com.example.travel_backend.dto.response.ItineraryItemResponseDto> getItineraryItems(UUID itineraryId, UUID requesterId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        if (itinerary.getIsPublic() != null && itinerary.getIsPublic()) {
            return itineraryItemRepository.findByItineraryIdOrderByOrderIndexAsc(itineraryId)
                    .stream()
                    .map(this::mapItemToDto)
                    .collect(Collectors.toList());
        }

        boolean isOwner = requesterId != null && itinerary.getUser().getId().equals(requesterId);
        boolean isCollaborator = isOwner;
        if (!isOwner && requesterId != null) {
            com.example.travel_backend.entity.User user = userRepository.findById(requesterId).orElse(null);
            if (user != null) {
                String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
                if (!email.isEmpty()) {
                    isCollaborator = collaboratorRepository.findByItinerary_IdAndEmail(itineraryId, email).isPresent();
                }
            }
        }

        if (!isCollaborator) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to view items of this itinerary");
        }

        return itineraryItemRepository.findByItineraryIdOrderByOrderIndexAsc(itineraryId)
                .stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
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

    @org.springframework.transaction.annotation.Transactional
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

        dto.setScheduledTime(item.getScheduledTime() != null ? item.getScheduledTime().toString() : null);
        dto.setDay(item.getDay());
        dto.setNote(item.getNote());
        dto.setOrderIndex(item.getOrderIndex());
        dto.setWarningType(item.getWarningType());
        dto.setWarningValue(item.getWarningValue());
        return dto;
    }

    public ItineraryResponseDto getItineraryById(UUID itineraryId, UUID requesterId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        boolean isOwner = itinerary.getUser().getId().equals(requesterId);
        String role = "VIEW";
        String email = "";
        boolean isCollaborator = false;
        
        com.example.travel_backend.entity.User user = userRepository.findById(requesterId).orElse(null);
        if (user != null) {
            email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        }

        if (isOwner) {
            role = "OWNER";
            isCollaborator = true;
        } else if (!email.isEmpty()) {
            var collabOpt = collaboratorRepository.findByItinerary_IdAndEmail(itineraryId, email);
            if (collabOpt.isPresent()) {
                role = collabOpt.get().getRole();
                isCollaborator = true;
            }
        }

        if (!itinerary.getIsPublic() && !isCollaborator) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to view this itinerary");
        }

        long itemCount = itineraryItemRepository.findByItineraryIdOrderByOrderIndexAsc(itineraryId).size();

        return mapToDto(itinerary, requesterId, itemCount, role);
    }

    // PURE FUNCTION: Đã loại bỏ hoàn toàn các lời gọi đến Repository bên trong hàm này
    private ItineraryResponseDto mapToDto(Itinerary i, UUID requesterId, long itemCount, String collaboratorRole) {
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
        dto.setStatus(i.getStatus());
        dto.setIsPublic(i.getIsPublic());
        
        // i.getUser() sẽ không bị LazyInitializationException vì đã có JOIN FETCH ở query
        dto.setOwnerId(i.getUser().getId());
        dto.setItemCount((int) itemCount);

        if (requesterId == null || i.getUser().getId().equals(requesterId)) {
            dto.setMyRole("OWNER");
        } else {
            dto.setMyRole(collaboratorRole);
        }
        return dto;
    }
}