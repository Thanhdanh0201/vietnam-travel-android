package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.EventDto;
import com.example.travel_backend.entity.Province;

import java.util.List;

public interface ProvinceService {
    List<Province> getAllProvinces();
    Province getProvinceByCode(String code);
    List<Province> searchProvinces(String query, int limit);
    List<EventDto> getEventsByProvinceCode(String code);
    List<com.example.travel_backend.dto.response.CityDto> getCitiesByProvinceCode(String code);
}