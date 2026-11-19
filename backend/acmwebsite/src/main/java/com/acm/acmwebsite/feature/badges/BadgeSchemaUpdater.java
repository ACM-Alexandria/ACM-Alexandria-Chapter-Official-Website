package com.acm.acmwebsite.feature.badges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// Hibernate's ddl-auto=update adds new columns but never relaxes existing ones. Club board badges have no
// committee, so badge_awards.committee_id, created NOT NULL before clubs could win badges, is made nullable
// here once, at startup after the schema update; it does nothing when the column is already nullable.
@Component
@Order(0)
@RequiredArgsConstructor
public class BadgeSchemaUpdater implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BadgeSchemaUpdater.class);

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String nullable = jdbc.queryForObject(
                    "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS "
                            + "WHERE LOWER(TABLE_NAME) = 'badge_awards' AND LOWER(COLUMN_NAME) = 'committee_id' "
                            + "AND TABLE_SCHEMA = DATABASE()",
                    String.class);
            if ("NO".equalsIgnoreCase(nullable)) {
                jdbc.execute("ALTER TABLE badge_awards MODIFY committee_id BIGINT NULL");
                log.info("badge_awards.committee_id is now nullable (club board badges have no committee)");
            }
        } catch (Exception e) {
            // No such table/column yet, or a database without these views: nothing to change.
            log.debug("Badge schema check skipped: {}", e.getMessage());
        }
    }
}
