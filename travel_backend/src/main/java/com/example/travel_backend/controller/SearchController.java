package com.example.travel_backend.controller;

import com.example.travel_backend.service.SearchKeywordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchKeywordService searchKeywordService;

    public SearchController(SearchKeywordService searchKeywordService) {
        this.searchKeywordService = searchKeywordService;
    }

    @PostMapping("/log")
    public ResponseEntity<Void> logKeyword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {

        String query = body.get("query");
        UUID userId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        searchKeywordService.logKeyword(query, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingKeywords(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        return ResponseEntity.ok(searchKeywordService.getTrendingKeywords(limit));
    }
}
