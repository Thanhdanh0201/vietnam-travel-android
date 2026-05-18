package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.*;
import com.example.travel_backend.entity.Event;
import com.example.travel_backend.entity.Place;
import com.example.travel_backend.entity.PlacePhoto;
import com.example.travel_backend.entity.PlaceTrending;
import com.example.travel_backend.repository.EventRepository;
import com.example.travel_backend.repository.PlacePhotoRepository;
import com.example.travel_backend.repository.PlaceRepository;
import com.example.travel_backend.repository.PlaceTrendingRepository;
import com.example.travel_backend.service.PlaceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

    public PlaceServiceImpl(PlaceRepository placeRepository,
                            PlacePhotoRepository placePhotoRepository,
                            EventRepository eventRepository,
                            PlaceTrendingRepository placeTrendingRepository) {
        this.placeRepository = placeRepository;
        this.placePhotoRepository = placePhotoRepository;
        this.eventRepository = eventRepository;
        this.placeTrendingRepository = placeTrendingRepository;
    }

    @Override
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
        List<EventDto> eventDtos = events.stream().map(e -> {
            EventDto dto = new EventDto();
            dto.setId(e.getId());
            dto.setName(e.getName());
            dto.setDescription(e.getDescription());
            dto.setType(e.getType());
            dto.setStart_date(e.getStartDate() != null ? e.getStartDate().toString() : null);
            dto.setEnd_date(e.getEndDate() != null ? e.getEndDate().toString() : null);
            if (e.getProvince() != null) {
                dto.setProvince_name(e.getProvince().getName());
            }
            if (e.getPlace() != null) {
                dto.setPlace_name(e.getPlace().getName());
            }
            return dto;
        }).collect(Collectors.toList());
        response.setEvents(eventDtos);

        return response;
    }

    @Override
    public List<PlaceResponse> getPlaces(String provinceCode, String type, int limit) {
        System.out.println("Get places");
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Place> places;
        if (provinceCode != null && type != null) {
            places = placeRepository.findByProvince_CodeAndType(provinceCode, type, pageRequest);
        } else if (provinceCode != null) {
            places = placeRepository.findByProvince_Code(provinceCode, pageRequest);
        } else {
            places = placeRepository.findAll(pageRequest).getContent();
        }
        return places.stream().map(this::mapToPlaceResponse).collect(Collectors.toList());
    }

    @Override
    public List<PlaceTrending> getTrendingPlaces(int limit) {
        System.out.println("Get trending places");
        PageRequest pageRequest = PageRequest.of(0, limit);
        return placeTrendingRepository.findAllByOrderByScoreDesc(pageRequest);
    }

    @Override
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
}