package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.UpdateItineraryDto;
import com.example.travel_backend.dto.response.ItineraryResponseDto;
import com.example.travel_backend.service.impl.ItineraryServiceImpl;
import com.example.travel_backend.repository.ItineraryCollaboratorRepository;
import com.example.travel_backend.repository.ItineraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    @Autowired
    private ItineraryServiceImpl itineraryService;

    @Autowired
    private ItineraryCollaboratorRepository collaboratorRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private com.example.travel_backend.repository.UserRepository userRepository;

    public static class CollaboratorDto {
        private String email;
        private String name;
        private String role;

        public CollaboratorDto() {}

        public CollaboratorDto(String email, String name, String role) {
            this.email = email;
            this.name = name;
            this.role = role;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
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
                    isCollaborator = collaboratorRepository.findByItinerary_IdAndEmail(itineraryId, email).isPresent();
                }
            }
        }

        if (!isCollaborator && (itinerary.getIsPublic() == null || !itinerary.getIsPublic())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to view collaborators");
        }

        List<com.example.travel_backend.entity.ItineraryCollaborator> list = collaboratorRepository.findByItinerary_Id(itineraryId);
        List<CollaboratorDto> dtoList = list.stream()
                .map(c -> new CollaboratorDto(c.getEmail(), c.getName(), c.getRole()))
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
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Email is required");
        }

        // Tên hiển thị: nếu không có thì dùng phần trước '@' của email
        String displayName = (request.getName() != null && !request.getName().isBlank())
                ? request.getName().trim()
                : request.getEmail().split("@")[0];

        com.example.travel_backend.entity.ItineraryCollaborator collaborator =
                collaboratorRepository.findByItinerary_IdAndEmail(itineraryId, request.getEmail().trim())
                .orElseGet(() -> {
                    com.example.travel_backend.entity.ItineraryCollaborator c = new com.example.travel_backend.entity.ItineraryCollaborator();
                    c.setItinerary(itinerary);
                    c.setEmail(request.getEmail().trim().toLowerCase());
                    return c;
                });
        collaborator.setName(displayName);
        collaborator.setRole(request.getRole() != null ? request.getRole().toUpperCase() : "VIEW");

        collaboratorRepository.save(collaborator);

        return ResponseEntity.ok(new CollaboratorDto(collaborator.getEmail(), collaborator.getName(), collaborator.getRole()));
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
}