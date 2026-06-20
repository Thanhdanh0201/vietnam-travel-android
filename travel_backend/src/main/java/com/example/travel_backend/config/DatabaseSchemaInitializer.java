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
    public void ensureNotificationSoftDeleteColumn() {
        if (jdbcTemplate == null) return;
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE notifications ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false"
            );
            log.info("Ensured notifications.is_deleted column exists");
        } catch (Exception e) {
            log.error("Could not ensure notifications.is_deleted column", e);
        }
    }
}
