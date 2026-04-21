package com.example.travel_backend.service.impl;

import com.example.travel_backend.entity.Event;
import com.example.travel_backend.entity.Province;
import com.example.travel_backend.repository.EventRepository;
import com.example.travel_backend.repository.ProvinceRepository;
import com.example.travel_backend.service.ProvinceService;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<Event> getEventsByProvinceCode(String code) {
        System.out.println("Get events by province code: " + code);
        return eventRepository.findByProvince_Code(code);
    }
}