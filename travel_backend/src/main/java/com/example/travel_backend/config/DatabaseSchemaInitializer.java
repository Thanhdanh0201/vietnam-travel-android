package com.example.travel_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureSoftDeleteColumns() {
        if (jdbcTemplate == null) return;
        ensureColumn("notifications", "is_deleted", "boolean NOT NULL DEFAULT false");
        ensureColumn("posts", "is_deleted", "boolean NOT NULL DEFAULT false");
        ensureColumn("comments", "is_deleted", "boolean NOT NULL DEFAULT false");
    }

    private void ensureColumn(String table, String column, String definition) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + column + " " + definition
            );
            log.info("Ensured {}.{} column exists", table, column);
        } catch (Exception e) {
            log.error("Could not ensure {}.{} column", table, column, e);
        }
    }
}
