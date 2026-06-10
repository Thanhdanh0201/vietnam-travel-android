package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.CreatePlaceReviewRequest;
import com.example.travel_backend.dto.response.*;
import com.example.travel_backend.entity.Event;
import com.example.travel_backend.entity.Place;
import com.example.travel_backend.entity.PlacePhoto;
import com.example.travel_backend.entity.PlaceTrending;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.entity.UserRating;
import com.example.travel_backend.mapper.EventMapper;
import com.example.travel_backend.repository.EventRepository;
import com.example.travel_backend.repository.PlacePhotoRepository;
import com.example.travel_backend.repository.PlaceRepository;
import com.example.travel_backend.repository.PlaceTrendingRepository;
import com.example.travel_backend.repository.UserRatingRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.PlaceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final PlacePhotoRepository placePhotoRepository;
    private final EventRepository eventRepository;
    private final PlaceTrendingRepository placeTrendingRepository;
    private final UserRatingRepository userRatingRepository;
    private final UserRepository userRepository;

    public PlaceServiceImpl(PlaceRepository placeRepository,
                            PlacePhotoRepository placePhotoRepository,
                            EventRepository eventRepository,
                            PlaceTrendingRepository placeTrendingRepository,
                            UserRatingRepository userRatingRepository,
                            UserRepository userRepository) {
        this.placeRepository = placeRepository;
        this.placePhotoRepository = placePhotoRepository;
        this.eventRepository = eventRepository;
        this.placeTrendingRepository = placeTrendingRepository;
        this.userRatingRepository = userRatingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceResponse> getPlacesByProvince(String provinceCode, String type, int limit) {
        System.out.println("Get places by province: " + provinceCode);
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Place> places;

        if (type != null && !type.isEmpty()) {
            places = placeRepository.findByProvince_CodeAndType(provinceCode, type, pageRequest);
        } else {
            places = placeRepository.findByProvince_Code(provinceCode, pageRequest);
        }

        return places.stream().map(this::mapToPlaceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(UUID id) {
        System.out.println("Get place detail for id: " + id);

        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Place not found"));

        PlaceDetailResponse response = new PlaceDetailResponse();
        response.setId(place.getId());
        response.setName(place.getName());
        response.setDescription(place.getDescription());
        response.setLat(place.getLat());
        response.setLng(place.getLng());
        response.setType(place.getType());
        response.setRating(place.getRating());
        response.setReview_count(place.getReviewCount());
        response.setApp_rating(place.getAppRating());
        response.setApp_review_count(place.getAppReviewCount());
        response.setImage_url(place.getImageUrl());
        response.setOpening_hours(place.getOpeningHours());

        ProvinceDto provinceDto = new ProvinceDto();
        if (place.getProvince() != null) {
            provinceDto.setName(place.getProvince().getName());
            provinceDto.setCode(place.getProvince().getCode());
        }
        response.setProvinces(provinceDto);

        CityDto cityDto = new CityDto();
        if (place.getCity() != null) {
            cityDto.setName(place.getCity().getName());
        }
        response.setCities(cityDto);

        List<PlacePhoto> photos = placePhotoRepository.findByPlace_Id(id);
        List<PhotoDto> photoDtos = photos.stream().map(p -> {
            PhotoDto dto = new PhotoDto();
            dto.setId(p.getId());
            dto.setPhoto_url(p.getPhotoUrl());
            dto.setIs_primary(p.getIsPrimary());
            return dto;
        }).collect(Collectors.toList());
        response.setPhotos(photoDtos);

        List<Event> events = eventRepository.findByPlace_Id(id);
        List<EventDto> eventDtos = events.stream()
                .map(EventMapper::toDto)
                .collect(Collectors.toList());
        response.setEvents(eventDtos);

        List<PlaceReviewDto> reviewDtos = userRatingRepository
                .findByPlace_IdOrderByCreatedAtDesc(id, PageRequest.of(0, 30))
                .stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());
        response.setReviews(reviewDtos);

        return response;
    }

    @Override
    @Transactional
    public PlaceReviewDto createReview(UUID placeId, UUID userId, CreatePlaceReviewRequest request) {
        if (request == null || request.getRating() == null
                || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        User user = ensureUserExists(userId);

        UserRating rating = new UserRating();
        rating.setPlace(place);
        rating.setUser(user);
        rating.setRating(request.getRating().shortValue());
        rating.setReview(request.getReview() != null ? request.getReview().trim() : null);
        rating.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userRatingRepository.save(rating);

        List<String> urls = request.getPhoto_urls() != null ? request.getPhoto_urls() : List.of();
        int savedPhotos = 0;
        for (String url : urls) {
            if (url == null || url.isBlank() || savedPhotos >= 3) continue;
            PlacePhoto photo = new PlacePhoto();
            photo.setPlace(place);
            photo.setUser(userRepository.getReferenceById(userId));
            photo.setPhotoUrl(url.trim());
            photo.setIsPrimary(false);
            placePhotoRepository.save(photo);
            savedPhotos++;
        }

        return mapToReviewDto(rating, placeId);
    }

    private User ensureUserExists(UUID userId) {
        return userRepository.findById(userId).orElseGet(() -> {
            User created = new User();
            created.setId(userId);
            created.setEmail(userId + "@traveler.local");
            created.setName("Khách du lịch");
            created.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            created.setIsVerified(true);
            return userRepository.save(created);
        });
    }

    private PlaceReviewDto mapToReviewDto(UserRating rating) {
        UUID placeId = rating.getPlace() != null ? rating.getPlace().getId() : null;
        return mapToReviewDto(rating, placeId);
    }

    private PlaceReviewDto mapToReviewDto(UserRating rating, UUID placeId) {
        PlaceReviewDto dto = new PlaceReviewDto();
        if (rating.getUser() != null) {
            dto.setUser_name(rating.getUser().getName());
            dto.setUser_avatar_url(rating.getUser().getAvatarUrl());
        }
        dto.setReview(rating.getReview());
        dto.setRating(rating.getRating());
        dto.setCreatedAt(rating.getCreatedAt());
        if (placeId != null && rating.getUser() != null) {
            List<String> photos = placePhotoRepository
                    .findByPlace_IdAndUser_IdOrderByUploadedAtDesc(placeId, rating.getUser().getId())
                    .stream()
                    .map(PlacePhoto::getPhotoUrl)
                    .limit(3)
                    .collect(Collectors.toList());
            dto.setPhoto_urls(photos);
        } else {
            dto.setPhoto_urls(List.of());
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceResponse> getPlaces(String provinceCode, String type, int limit) {
        System.out.println("Get places");
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Place> places;
        if (provinceCode != null && type != null) {
            places = placeRepository.findByProvince_CodeAndType(provinceCode, type, pageRequest);
        } else if (provinceCode != null) {
            places = placeRepository.findByProvince_Code(provinceCode, pageRequest);
        } else {
            // Gợi ý trang chủ: rating cao nhất trước (bảng places.rating)
            PageRequest byRating = PageRequest.of(
                    0,
                    limit,
                    Sort.by(
                            Sort.Order.desc("rating"),
                            Sort.Order.desc("reviewCount"),
                            Sort.Order.asc("name")
                    )
            );
            places = placeRepository.findAll(byRating).getContent();
        }
        return places.stream().map(this::mapToPlaceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.example.travel_backend.dto.response.PlaceTrendingResponseDto> getTrendingPlaces(int limit) {
        System.out.println("Get trending places");
        PageRequest pageRequest = PageRequest.of(0, limit);
        return placeTrendingRepository.findAllByOrderByScoreDesc(pageRequest)
                .stream()
                .map(this::mapToTrendingDto)
                .collect(Collectors.toList());
    }

    private com.example.travel_backend.dto.response.PlaceTrendingResponseDto mapToTrendingDto(PlaceTrending trending) {
        com.example.travel_backend.dto.response.PlaceTrendingResponseDto dto = new com.example.travel_backend.dto.response.PlaceTrendingResponseDto();
        dto.setPlace_id(trending.getPlace().getId());
        dto.setProvince_id(trending.getProvince().getId());
        dto.setRank_position(trending.getRankPosition());
        dto.setTotal_searches(trending.getScore());
        
        com.example.travel_backend.dto.response.PlaceTrendingResponseDto.PlaceDetailCompactDto placeDto = 
                new com.example.travel_backend.dto.response.PlaceTrendingResponseDto.PlaceDetailCompactDto();
        placeDto.setName(trending.getPlace().getName());
        placeDto.setImage_url(trending.getPlace().getImageUrl());
        placeDto.setType(trending.getPlace().getType());
        placeDto.setLat(trending.getPlace().getLat());
        placeDto.setLng(trending.getPlace().getLng());
        dto.setPlaces(placeDto);
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlacePhoto> getPlacePhotos(UUID id) {
        System.out.println("Get photos for place: " + id);
        return placePhotoRepository.findByPlace_Id(id);
    }

    @Override
    public Map<String, String> logPlaceAction(UUID id, String actionType, UUID userId) {
        System.out.println("Log action: " + actionType + " for place: " + id);
        return Map.of("status", "logged");
    }

    private PlaceResponse mapToPlaceResponse(Place place) {
        PlaceResponse response = new PlaceResponse();
        response.setId(place.getId());
        response.setName(place.getName());
        response.setLat(place.getLat());
        response.setLng(place.getLng());
        response.setType(place.getType());
        response.setRating(place.getRating());
        response.setReview_count(place.getReviewCount());
        response.setImage_url(place.getImageUrl());

        ProvinceDto provinceDto = new ProvinceDto();
        if (place.getProvince() != null) {
            provinceDto.setName(place.getProvince().getName());
            provinceDto.setCode(place.getProvince().getCode());
        }
        response.setProvinces(provinceDto);

        CityDto cityDto = new CityDto();
        if (place.getCity() != null) {
            cityDto.setName(place.getCity().getName());
        }
        response.setCities(cityDto);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceResponse> searchPlaces(String query, int limit) {
        System.out.println("Search places by name: " + query);
        PageRequest pageRequest = PageRequest.of(0, limit);
        return placeRepository.findByNameContainingIgnoreCase(query, pageRequest)
                .getContent()
                .stream()
                .map(this::mapToPlaceResponse)
                .collect(Collectors.toList());
    }
}