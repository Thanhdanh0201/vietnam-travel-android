package com.example.travel_backend.service;

import com.example.travel_backend.entity.Event;
import com.example.travel_backend.entity.Province;

import java.util.List;

public interface ProvinceService {
    List<Province> getAllProvinces();
    Province getProvinceByCode(String code);
    List<Event> getEventsByProvinceCode(String code);
}