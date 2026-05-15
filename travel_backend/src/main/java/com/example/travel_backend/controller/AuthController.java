package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.SyncUserRequestDto;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.entity.UserSetting;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;


import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository; // Thêm dòng này

    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(@RequestBody SyncUserRequestDto request) {
        Optional<User> existingUser = userRepository.findById(request.getId());
        if (existingUser.isPresent()) {
            return ResponseEntity.ok(existingUser.get());
        }

        User newUser = new User();
        newUser.setId(request.getId());
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getName());
        newUser.setCreatedAt(OffsetDateTime.now());
        newUser.setIsVerified(true);
        userRepository.save(newUser);


        UserSetting newSettings = new UserSetting();
        newSettings.setUser(newUser);
        userSettingsRepository.save(newSettings);

        return ResponseEntity.ok(newUser);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(404).body("Không tìm thấy User trong Database");
        }
    }
}
