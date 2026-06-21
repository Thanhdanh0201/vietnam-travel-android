package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.UpdateItineraryDto;
import com.example.travel_backend.dto.response.ItineraryResponseDto;
import com.example.travel_backend.service.NotificationTriggerService;
import com.example.travel_backend.service.impl.ItineraryServiceImpl;
import com.example.travel_backend.repository.ItineraryCollaboratorRepository;
import com.example.travel_backend.repository.ItineraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private static final Logger log = LoggerFactory.getLogger(ItineraryController.class);

    @Autowired
    private ItineraryServiceImpl itineraryService;

    @Autowired
    private ItineraryCollaboratorRepository collaboratorRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private com.example.travel_backend.repository.UserRepository userRepository;

    @Autowired
    private NotificationTriggerService notificationTriggerService;

    public static class CollaboratorDto {
        private String email;
        private String name;
        private String role;
        private String status;
        private String userId;
        private String avatarUrl;

        public CollaboratorDto() {}

        public CollaboratorDto(String email, String name, String role) {
            this.email = email;
            this.name = name;
            this.role = role;
        }

        public CollaboratorDto(String email, String name, String role, String status) {
            this.email = email;
            this.name = name;
            this.role = role;
            this.status = status;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }

    private CollaboratorDto toCollaboratorDto(com.example.travel_backend.entity.ItineraryCollaborator collaborator) {
        CollaboratorDto dto = new CollaboratorDto(
                collaborator.getEmail(),
                collaborator.getName(),
                collaborator.getRole(),
                collaborator.getStatus()
        );
        userRepository.findByEmailIgnoreCase(collaborator.getEmail()).ifPresent(user -> {
            dto.setUserId(user.getId().toString());
            dto.setAvatarUrl(user.getAvatarUrl());
        });
        return dto;
    }


    // GET /api/itineraries?user_id=...&is_public=true&limit=20&offset=0
    @GetMapping
    public ResponseEntity<List<ItineraryResponseDto>> getPublicItineraries(
            @RequestParam("user_id") UUID userId,
            @RequestParam(value = "is_public", defaultValue = "true") Boolean isPublic,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        return ResponseEntity.ok(itineraryService.getPublicItineraries(userId, limit, offset));
    }

    // PATCH /api/itineraries?id={itinerary_id}
    @PatchMapping
    public ResponseEntity<Void> updateItinerary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("id") UUID itineraryId,
            @RequestBody UpdateItineraryDto request) {

        UUID requesterId = UUID.fromString(jwt.getSubject());

        itineraryService.updateItineraryStatus(itineraryId, requesterId, request);

        return ResponseEntity.ok().build();
    }

    // POST /api/itineraries
    @PostMapping
    public ResponseEntity<ItineraryResponseDto> createItinerary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody com.example.travel_backend.dto.request.CreateItineraryRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.createItinerary(myId, request));
    }

    // DELETE /api/itineraries
    @DeleteMapping
    public ResponseEntity<Void> deleteItinerary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("id") UUID itineraryId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        itineraryService.deleteItinerary(itineraryId, myId);
        return ResponseEntity.ok().build();
    }

    // GET /api/itineraries/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ItineraryResponseDto> getItineraryById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.getItineraryById(itineraryId, myId));
    }

    // GET /api/itineraries/me
    @GetMapping("/me")
    public ResponseEntity<List<ItineraryResponseDto>> getMyItineraries(
            @AuthenticationPrincipal Jwt jwt) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.getMyItineraries(myId));
    }

    // GET /api/itineraries/{id}/items
    @GetMapping("/{id}/items")
    public ResponseEntity<List<com.example.travel_backend.dto.response.ItineraryItemResponseDto>> getItineraryItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId) {

        UUID myId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return ResponseEntity.ok(itineraryService.getItineraryItems(itineraryId, myId));
    }

    // POST /api/itineraries/{id}/items
    @PostMapping("/{id}/items")
    public ResponseEntity<com.example.travel_backend.dto.response.ItineraryItemResponseDto> addItineraryItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @RequestBody com.example.travel_backend.dto.request.CreateItineraryItemRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.addItineraryItem(itineraryId, myId, request));
    }

    // DELETE /api/itineraries/{id}/items/{itemId}
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> deleteItineraryItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @PathVariable("itemId") UUID itemId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        itineraryService.deleteItineraryItem(itineraryId, itemId, myId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/collaborators")
    public ResponseEntity<List<CollaboratorDto>> getCollaborators(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId) {

        UUID myId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;

        com.example.travel_backend.entity.Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        boolean isOwner = myId != null && itinerary.getUser().getId().equals(myId);
        boolean isCollaborator = isOwner;
        if (!isOwner && myId != null) {
            com.example.travel_backend.entity.User user = userRepository.findById(myId).orElse(null);
            if (user != null) {
                String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
                if (!email.isEmpty()) {
                    isCollaborator = collaboratorRepository.findAcceptedByItinerary_IdAndEmail(itineraryId, email).isPresent();
                }
            }
        }

        if (!isCollaborator && (itinerary.getIsPublic() == null || !itinerary.getIsPublic())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to view collaborators");
        }

        List<com.example.travel_backend.entity.ItineraryCollaborator> list = collaboratorRepository.findByItinerary_Id(itineraryId);
        List<CollaboratorDto> dtoList = list.stream()
                .map(this::toCollaboratorDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{id}/collaborators")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<CollaboratorDto> addCollaborator(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @RequestBody CollaboratorDto request) {

        UUID requesterId = UUID.fromString(jwt.getSubject());

        com.example.travel_backend.entity.Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));

        // Chỉ OWNER mới được thêm collaborator
        if (!itinerary.getUser().getId().equals(requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Only the owner can manage collaborators");
        }

        // Không cho phép add chính mình làm collaborator
        String resolvedEmail = request.getEmail();
        String displayName = request.getName();
        UUID invitedUserId = null;

        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            UUID inviteUserId = UUID.fromString(request.getUserId().trim());
            if (inviteUserId.equals(requesterId)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Cannot invite yourself");
            }
            com.example.travel_backend.entity.User invitedUser = userRepository.findById(inviteUserId)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
            if (invitedUser.getEmail() == null || invitedUser.getEmail().isBlank()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "User has no email");
            }
            invitedUserId = inviteUserId;
            resolvedEmail = invitedUser.getEmail().trim().toLowerCase();
            if (displayName == null || displayName.isBlank()) {
                displayName = invitedUser.getName();
            }
        }

        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Email or userId is required");
        }

        // Tên hiển thị: nếu không có thì dùng phần trước '@' của email
        displayName = (displayName != null && !displayName.isBlank())
                ? displayName.trim()
                : resolvedEmail.split("@")[0];

        final String finalEmail = resolvedEmail;

        com.example.travel_backend.entity.ItineraryCollaborator collaborator =
                collaboratorRepository.findByItinerary_IdAndEmail(itineraryId, finalEmail.trim())
                .orElseGet(() -> {
                    com.example.travel_backend.entity.ItineraryCollaborator c = new com.example.travel_backend.entity.ItineraryCollaborator();
                    c.setItinerary(itinerary);
                    c.setEmail(finalEmail.trim().toLowerCase());
                    return c;
                });
        collaborator.setName(displayName);
        collaborator.setRole(request.getRole() != null ? request.getRole().toUpperCase() : "VIEW");
        collaborator.setStatus("pending");
        collaborator.setInvitedBy(requesterId);
        collaborator.setInvitedAt(OffsetDateTime.now());
        collaborator.setRespondedAt(null);

        collaboratorRepository.save(collaborator);

        try {
            if (invitedUserId != null) {
                notificationTriggerService.notifyItineraryInvite(requesterId, invitedUserId, itineraryId);
            } else {
                userRepository.findByEmailIgnoreCase(collaborator.getEmail()).ifPresent(invitedUser ->
                        notificationTriggerService.notifyItineraryInvite(requesterId, invitedUser.getId(), itineraryId));
            }
        } catch (Exception e) {
            log.error("Failed to send itinerary invite notification for collaborator: {}", collaborator.getEmail(), e);
        }

        return ResponseEntity.ok(toCollaboratorDto(collaborator));
    }

    @PostMapping("/{id}/invites/accept")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> acceptInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId) {

        UUID requesterId = UUID.fromString(jwt.getSubject());
        com.example.travel_backend.entity.User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "User email is missing");
        }

        com.example.travel_backend.entity.ItineraryCollaborator collaborator =
                collaboratorRepository.findByItinerary_IdAndEmail(itineraryId, email)
                        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND, "Invite not found"));

        if (!"pending".equalsIgnoreCase(collaborator.getStatus())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invite is already handled");
        }
        collaborator.setStatus("accepted");
        collaborator.setRespondedAt(OffsetDateTime.now());
        collaboratorRepository.save(collaborator);
        notificationTriggerService.resolveItineraryInviteNotifications(requesterId, itineraryId, "accepted");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/invites/decline")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> declineInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId) {

        UUID requesterId = UUID.fromString(jwt.getSubject());
        com.example.travel_backend.entity.User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "User email is missing");
        }

        com.example.travel_backend.entity.ItineraryCollaborator collaborator =
                collaboratorRepository.findByItinerary_IdAndEmail(itineraryId, email)
                        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND, "Invite not found"));

        if (!"pending".equalsIgnoreCase(collaborator.getStatus())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invite is already handled");
        }
        collaborator.setStatus("rejected");
        collaborator.setRespondedAt(OffsetDateTime.now());
        collaboratorRepository.save(collaborator);
        notificationTriggerService.resolveItineraryInviteNotifications(requesterId, itineraryId, "declined");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/collaborators/{email}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> removeCollaborator(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @PathVariable("email") String email) {

        UUID requesterId = UUID.fromString(jwt.getSubject());

        // Chỉ OWNER mới được xóa collaborator
        com.example.travel_backend.entity.Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Itinerary not found"));
        if (!itinerary.getUser().getId().equals(requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Only the owner can manage collaborators");
        }

        collaboratorRepository.deleteByItinerary_IdAndEmail(itineraryId, email.trim());
        return ResponseEntity.ok().build();
    }

    // ===================== ITINERARY NOTES =====================

    /**
     * GET /api/itineraries/{id}/notes?item_id=
     * Lấy danh sách ghi chú nhóm.
     * - ?item_id=xxx → ghi chú của địa điểm cụ thể
     * - không có item_id → ghi chú chung của cả lịch trình
     */
    @GetMapping("/{id}/notes")
    public ResponseEntity<List<com.example.travel_backend.dto.response.ItineraryNoteResponseDto>> getItineraryNotes(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @RequestParam(value = "item_id", required = false) UUID itemId) {

        UUID requesterId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return ResponseEntity.ok(itineraryService.getItineraryNotes(itineraryId, itemId, requesterId));
    }

    /**
     * POST /api/itineraries/{id}/notes
     * Thêm ghi chú nhóm mới — tất cả thành viên.
     */
    @PostMapping("/{id}/notes")
    public ResponseEntity<com.example.travel_backend.dto.response.ItineraryNoteResponseDto> addItineraryNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @RequestBody com.example.travel_backend.dto.request.ItineraryNoteRequestDto request) {

        UUID requesterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.addItineraryNote(itineraryId, requesterId, request));
    }

    /**
     * DELETE /api/itineraries/{id}/notes/{noteId}
     * Xóa ghi chú — chỉ tác giả hoặc owner.
     */
    @DeleteMapping("/{id}/notes/{noteId}")
    public ResponseEntity<Void> deleteItineraryNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @PathVariable("noteId") UUID noteId) {

        UUID requesterId = UUID.fromString(jwt.getSubject());
        itineraryService.deleteItineraryNote(itineraryId, noteId, requesterId);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH /api/itineraries/{id}/items/{itemId}
     * Cập nhật ghi chú riêng của địa điểm trong lịch trình.
     */
    @PatchMapping("/{id}/items/{itemId}")
    public ResponseEntity<com.example.travel_backend.dto.response.ItineraryItemResponseDto> updateItineraryItemNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @PathVariable("itemId") UUID itemId,
            @RequestBody com.example.travel_backend.dto.request.UpdateItineraryItemDto request) {

        UUID requesterId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.updateItineraryItemNote(itineraryId, itemId, requesterId, request));
    }
}