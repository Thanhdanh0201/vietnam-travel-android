package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.UpdateItineraryDto;
import com.example.travel_backend.dto.response.ItineraryResponseDto;
import com.example.travel_backend.entity.Itinerary;
import com.example.travel_backend.repository.ItineraryRepository;
import com.example.travel_backend.service.ItineraryService;
import com.example.travel_backend.service.NotificationTriggerService;
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

    @Autowired
    private com.example.travel_backend.repository.ItineraryNoteRepository itineraryNoteRepository;

    @Autowired
    private NotificationTriggerService notificationTriggerService;

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
                    return collaboratorRepository.findAcceptedByItinerary_IdAndEmail(itinerary.getId(), email.trim())
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
            notificationTriggerService.notifyItineraryUpdated(requesterId, itineraryId);
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
                    isCollaborator = collaboratorRepository.findAcceptedByItinerary_IdAndEmail(itineraryId, email).isPresent();
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
            var collabOpt = collaboratorRepository.findAcceptedByItinerary_IdAndEmail(itineraryId, email);
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

    // =================== ITINERARY NOTES ===================

    /**
     * Lấy ghi chú nhóm:
     * - itemId != null → ghi chú của địa điểm cụ thể
     * - itemId == null → ghi chú chung của cả lịch trình
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<com.example.travel_backend.dto.response.ItineraryNoteResponseDto> getItineraryNotes(
            UUID itineraryId, UUID itemId, UUID requesterId) {

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        // Kiểm tra quyền truy cập: phải là thành viên hoặc lịch trình public
        if (!itinerary.getIsPublic()) {
            requireMembership(itinerary, requesterId);
        }

        List<com.example.travel_backend.entity.ItineraryNote> notes;
        if (itemId != null) {
            notes = itineraryNoteRepository
                    .findByItinerary_IdAndItineraryItem_IdOrderByCreatedAtAsc(itineraryId, itemId);
        } else {
            notes = itineraryNoteRepository
                    .findByItinerary_IdAndItineraryItemIsNullOrderByCreatedAtAsc(itineraryId);
        }
        return notes.stream().map(this::mapNoteToDto).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Thêm ghi chú nhóm — tất cả thành viên đều được phép.
     */
    @org.springframework.transaction.annotation.Transactional
    public com.example.travel_backend.dto.response.ItineraryNoteResponseDto addItineraryNote(
            UUID itineraryId, UUID requesterId,
            com.example.travel_backend.dto.request.ItineraryNoteRequestDto request) {

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        // Kiểm tra: phải là thành viên (owner hoặc collaborator accepted)
        requireMembership(itinerary, requesterId);

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Content is required");
        }

        com.example.travel_backend.entity.User actor = userRepository.findById(requesterId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        com.example.travel_backend.entity.ItineraryNote note = new com.example.travel_backend.entity.ItineraryNote();
        note.setItinerary(itinerary);
        note.setUser(actor);
        note.setContent(request.getContent().trim());
        note.setImageUrl(request.getImageUrl());
        note.setCreatedAt(java.time.OffsetDateTime.now());

        // Gắn vào itinerary item nếu có
        if (request.getItineraryItemId() != null) {
            com.example.travel_backend.entity.ItineraryItem item =
                    itineraryItemRepository.findById(request.getItineraryItemId())
                            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                    org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary item not found"));
            if (!item.getItinerary().getId().equals(itineraryId)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Item does not belong to this itinerary");
            }
            note.setItineraryItem(item);
        }

        com.example.travel_backend.entity.ItineraryNote saved = itineraryNoteRepository.save(note);

        // Gửi notification cho các thành viên khác
        notificationTriggerService.notifyItineraryNote(requesterId, itineraryId, request.getContent());

        return mapNoteToDto(saved);
    }

    /**
     * Xóa ghi chú — chỉ tác giả hoặc owner lịch trình.
     */
    @org.springframework.transaction.annotation.Transactional
    public void deleteItineraryNote(UUID itineraryId, UUID noteId, UUID requesterId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        com.example.travel_backend.entity.ItineraryNote note = itineraryNoteRepository.findById(noteId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Note not found"));

        if (!note.getItinerary().getId().equals(itineraryId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Note does not belong to this itinerary");
        }

        boolean isAuthor = note.getUser().getId().equals(requesterId);
        boolean isOwner = itinerary.getUser().getId().equals(requesterId);

        if (!isAuthor && !isOwner) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Only the note author or itinerary owner can delete this note");
        }

        itineraryNoteRepository.delete(note);
    }

    /**
     * Cập nhật ghi chú riêng của một địa điểm (itinerary_items.note).
     */
    @org.springframework.transaction.annotation.Transactional
    public com.example.travel_backend.dto.response.ItineraryItemResponseDto updateItineraryItemNote(
            UUID itineraryId, UUID itemId, UUID requesterId,
            com.example.travel_backend.dto.request.UpdateItineraryItemDto request) {

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

        item.setNote(request.getNote());
        com.example.travel_backend.entity.ItineraryItem saved = itineraryItemRepository.save(item);
        return mapItemToDto(saved);
    }

    /**
     * Kiểm tra người dùng có phải thành viên của lịch trình không (owner hoặc collaborator accepted).
     */
    private void requireMembership(Itinerary itinerary, UUID requesterId) {
        boolean isOwner = itinerary.getUser().getId().equals(requesterId);
        if (isOwner) return;

        com.example.travel_backend.entity.User user = userRepository.findById(requesterId).orElse(null);
        if (user == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }
        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        boolean isCollaborator = !email.isEmpty() &&
                collaboratorRepository.findAcceptedByItinerary_IdAndEmail(itinerary.getId(), email).isPresent();

        if (!isCollaborator) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You are not a member of this itinerary");
        }
    }

    private com.example.travel_backend.dto.response.ItineraryNoteResponseDto mapNoteToDto(
            com.example.travel_backend.entity.ItineraryNote note) {
        com.example.travel_backend.dto.response.ItineraryNoteResponseDto dto =
                new com.example.travel_backend.dto.response.ItineraryNoteResponseDto();
        dto.setId(note.getId());
        dto.setItineraryId(note.getItinerary().getId());
        dto.setItineraryItemId(note.getItineraryItem() != null ? note.getItineraryItem().getId() : null);
        dto.setUserId(note.getUser().getId());
        dto.setUserName(note.getUser().getName() != null ? note.getUser().getName() : "Thành viên");
        dto.setUserAvatar(note.getUser().getAvatarUrl());
        dto.setContent(note.getContent());
        dto.setImageUrl(note.getImageUrl());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
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