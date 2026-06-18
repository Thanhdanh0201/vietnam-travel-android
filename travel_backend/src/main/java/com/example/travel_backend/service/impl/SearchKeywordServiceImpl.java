package com.example.travel_backend.service.impl;

import com.example.travel_backend.entity.SearchKeywordLog;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.SearchKeywordLogRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.SearchKeywordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SearchKeywordServiceImpl implements SearchKeywordService {

    private final SearchKeywordLogRepository searchKeywordLogRepository;
    private final UserRepository userRepository;

    public SearchKeywordServiceImpl(
            SearchKeywordLogRepository searchKeywordLogRepository,
            UserRepository userRepository) {
        this.searchKeywordLogRepository = searchKeywordLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void logKeyword(String query, UUID userId) {
        if (query == null || query.trim().length() < 2) return;

        SearchKeywordLog log = new SearchKeywordLog();
        log.setQuery(query.trim().toLowerCase());
        log.setCreatedAt(OffsetDateTime.now());

        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setUser);
        }

        searchKeywordLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTrendingKeywords(int limit) {
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        return searchKeywordLogRepository.findTopKeywords(since, limit);
    }
}
