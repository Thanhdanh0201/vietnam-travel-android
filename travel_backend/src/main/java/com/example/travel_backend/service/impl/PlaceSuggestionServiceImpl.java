package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.PlaceSuggestionRequestDto;
import com.example.travel_backend.dto.response.PlaceSuggestionResponseDto;
import com.example.travel_backend.entity.PlaceSuggestion;
import com.example.travel_backend.entity.Province;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.PlaceSuggestionRepository;
import com.example.travel_backend.repository.ProvinceRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.PlaceSuggestionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PlaceSuggestionServiceImpl implements PlaceSuggestionService {

    @Autowired private PlaceSuggestionRepository placeSuggestionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProvinceRepository provinceRepository;

    @Override
    @Transactional
    public PlaceSuggestionResponseDto createSuggestion(UUID userId, PlaceSuggestionRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (Boolean.TRUE.equals(user.getIsBanned())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Banned users cannot suggest places");
        }

        PlaceSuggestion suggestion = new PlaceSuggestion();
        suggestion.setUser(user);
        suggestion.setName(request.getName());
        suggestion.setLat(request.getLat());
        suggestion.setLng(request.getLng());
        suggestion.setType(request.getType());
        suggestion.setDescription(request.getDescription());
        suggestion.setImageUrl(request.getImageUrl());
        suggestion.setStatus("pending");
        suggestion.setCreatedAt(OffsetDateTime.now());

        if (request.getProvinceId() != null) {
            Province province = provinceRepository.findById(request.getProvinceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Province not found"));
            suggestion.setProvince(province);
        }

        PlaceSuggestion saved = placeSuggestionRepository.save(suggestion);
        return toSuggestionDto(saved);
    }

    @Override
    public Page<PlaceSuggestionResponseDto> getMySuggestions(UUID userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return placeSuggestionRepository.findByUser_Id(userId, pageable).map(this::toSuggestionDto);
    }

    private PlaceSuggestionResponseDto toSuggestionDto(PlaceSuggestion s) {
        PlaceSuggestionResponseDto dto = new PlaceSuggestionResponseDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setLat(s.getLat());
        dto.setLng(s.getLng());
        dto.setType(s.getType());
        dto.setDescription(s.getDescription());
        dto.setImageUrl(s.getImageUrl());
        dto.setStatus(s.getStatus());
        dto.setAdminNote(s.getAdminNote());
        dto.setReviewedAt(s.getReviewedAt());
        dto.setCreatedAt(s.getCreatedAt());
        if (s.getProvince() != null) {
            dto.setProvinceId(s.getProvince().getId());
            dto.setProvinceName(s.getProvince().getName());
        }
        if (s.getUser() != null) {
            dto.setUserId(s.getUser().getId());
            dto.setUserName(s.getUser().getName());
            dto.setUserAvatar(s.getUser().getAvatarUrl());
        }
        return dto;
    }
}
