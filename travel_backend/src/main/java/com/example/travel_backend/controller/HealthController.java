package com.example.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of("status", "ok");
    }

    @GetMapping("/health/db")
    public Map<String, Object> dbHealthCheck() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        if (jdbcTemplate == null) {
            result.put("schema", "jdbc_unavailable");
            return result;
        }
        List<String> columns = jdbcTemplate.query(
                """
                SELECT table_name || '.' || column_name
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name IN ('posts', 'comments')
                  AND column_name = 'is_deleted'
                ORDER BY table_name
                """,
                (rs, rowNum) -> rs.getString(1)
        );
        result.put("is_deleted_columns", columns);
        result.put("schema_ok", columns.size() == 2);
        return result;
    }
}