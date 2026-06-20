package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.UserInviteSearchDto;
import com.example.travel_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invite/members")
public class InviteMemberController {

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserInviteSearchDto>> searchMembers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.searchForInvite(myId, query, limit));
    }
}
