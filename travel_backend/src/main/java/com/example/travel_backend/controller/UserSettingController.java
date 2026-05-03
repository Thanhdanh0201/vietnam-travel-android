package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.UserSettingResponseDto;
import com.example.travel_backend.entity.UserSetting;
import com.example.travel_backend.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-settings")
public class UserSettingController {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMySettings(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        System.out.println("Fetching settings for user: " + userId); // Log in English

        Optional<UserSetting> settingsOpt = userSettingsRepository.findByUserId(userId);

        if (settingsOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User settings not found"); // Response in English
        }

        UserSetting settings = settingsOpt.get();
        UserSettingResponseDto dto = new UserSettingResponseDto();
        dto.setId(settings.getId());
        dto.setPushReactions(settings.getPushReactions());
        dto.setPushComments(settings.getPushComments());
        dto.setPushFollows(settings.getPushFollows());
        dto.setPushReposts(settings.getPushReposts());
        dto.setPushMentions(settings.getPushMentions());
        dto.setPushAchievements(settings.getPushAchievements());
        dto.setLanguage(settings.getLanguage());
        dto.setTheme(settings.getTheme());

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateSettings(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserSetting updatedData) {

        UUID userId = UUID.fromString(jwt.getSubject());
        System.out.println("Updating settings for user: " + userId);

        Optional<UserSetting> existingOpt = userSettingsRepository.findByUserId(userId);

        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Settings not found for update");
        }

        UserSetting existing = existingOpt.get();

        if (updatedData.getPushReactions() != null) existing.setPushReactions(updatedData.getPushReactions());
        if (updatedData.getPushComments() != null) existing.setPushComments(updatedData.getPushComments());
        if (updatedData.getPushFollows() != null) existing.setPushFollows(updatedData.getPushFollows());
        if (updatedData.getPushReposts() != null) existing.setPushReposts(updatedData.getPushReposts());
        if (updatedData.getPushMentions() != null) existing.setPushMentions(updatedData.getPushMentions());
        if (updatedData.getPushAchievements() != null) existing.setPushAchievements(updatedData.getPushAchievements());
        if (updatedData.getLanguage() != null) existing.setLanguage(updatedData.getLanguage());
        if (updatedData.getTheme() != null) existing.setTheme(updatedData.getTheme());

        userSettingsRepository.save(existing);

        return ResponseEntity.ok().build();
    }
}