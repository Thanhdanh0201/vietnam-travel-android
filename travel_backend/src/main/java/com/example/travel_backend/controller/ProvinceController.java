package com.example.travel_backend.controller;

import com.example.travel_backend.entity.Event;
import com.example.travel_backend.entity.Province;
import com.example.travel_backend.service.ProvinceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provinces")
public class ProvinceController {

    private final ProvinceService provinceService;

    public ProvinceController(ProvinceService provinceService) {
        this.provinceService = provinceService;
    }

    @GetMapping
    public ResponseEntity<List<Province>> getAllProvinces() {
        return ResponseEntity.ok(provinceService.getAllProvinces());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Province> getProvinceByCode(@PathVariable String code) {
        Province province = provinceService.getProvinceByCode(code);
        if (province != null) {
            return ResponseEntity.ok(province);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{code}/events")
    public ResponseEntity<List<Event>> getEventsByProvinceCode(@PathVariable String code) {
        return ResponseEntity.ok(provinceService.getEventsByProvinceCode(code));
    }
}