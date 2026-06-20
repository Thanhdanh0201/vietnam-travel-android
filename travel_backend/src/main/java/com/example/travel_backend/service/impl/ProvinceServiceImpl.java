package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.EventDto;
import com.example.travel_backend.entity.Province;
import com.example.travel_backend.mapper.EventMapper;
import com.example.travel_backend.repository.EventRepository;
import com.example.travel_backend.repository.ProvinceRepository;
import com.example.travel_backend.service.ProvinceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final EventRepository eventRepository;

    public ProvinceServiceImpl(ProvinceRepository provinceRepository, EventRepository eventRepository) {
        this.provinceRepository = provinceRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public List<Province> getAllProvinces() {
        System.out.println("Get all provinces");
        return provinceRepository.findAll();
    }

    @Override
    public Province getProvinceByCode(String code) {
        System.out.println("Get province by code: " + code);
        return provinceRepository.findByCode(code).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Province> searchProvinces(String query, int limit) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return provinceRepository.searchByNameOrNameEn(query.trim(), PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getEventsByProvinceCode(String code) {
        System.out.println("Get events by province code: " + code);
        return eventRepository.findByProvince_Code(code).stream()
                .map(EventMapper::toDto)
                .collect(Collectors.toList());
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.travel_backend.repository.CityRepository cityRepository;

    @Override
    public List<com.example.travel_backend.dto.response.CityDto> getCitiesByProvinceCode(String code) {
        System.out.println("Get cities by province code: " + code);
        return cityRepository.findByProvince_Code(code).stream()
                .map(city -> {
                    com.example.travel_backend.dto.response.CityDto dto = new com.example.travel_backend.dto.response.CityDto();
                    dto.setName(city.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}