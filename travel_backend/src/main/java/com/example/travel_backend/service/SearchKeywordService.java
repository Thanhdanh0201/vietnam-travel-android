package com.example.travel_backend.service;

import java.util.List;
import java.util.UUID;

public interface SearchKeywordService {
    void logKeyword(String query, UUID userId);
    List<String> getTrendingKeywords(int limit);
}
