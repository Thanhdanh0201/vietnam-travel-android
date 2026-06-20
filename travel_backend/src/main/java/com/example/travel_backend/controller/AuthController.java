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
            User user = existingUser.get();
            
            // Cập nhật tên từ request (ưu tiên tên thật người dùng nhập)
            if (request.getName() != null && !request.getName().isBlank()) {
                user.setName(request.getName());
            }
            
            // Cập nhật hoặc sinh username từ email nếu chưa có
            if (user.getUsername() == null || user.getUsername().isBlank()) {
                if (request.getEmail() != null && request.getEmail().contains("@")) {
                    user.setUsername(
                        request.getEmail().substring(0, request.getEmail().indexOf("@")).toLowerCase()
                    );
                }
            }
            
            // Luôn cập nhật is_verified thành true khi sync được gọi
            user.setIsVerified(true);
            
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }

        User newUser = new User();
        newUser.setId(request.getId());
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getName());
        if (request.getEmail() != null && request.getEmail().contains("@")) {
            newUser.setUsername(
                    request.getEmail().substring(0, request.getEmail().indexOf("@")).toLowerCase()
            );
        }
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
