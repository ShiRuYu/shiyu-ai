package com.shiyu.ai.dal.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs the repository's module-scoped migrations with an independent history
 * table per module. Migration version numbers intentionally repeat between
 * modules, so they cannot share Flyway's default history table.
 */
@Slf4j
@Configuration
public class EmbeddedFlywayConfiguration {

    private static final String[] MODULES = {
            "common", "auth", "vector", "knowledge", "memory",
            "education", "agent", "record", "observation"
    };

    @Bean("embeddedDatabaseMigrations")
    @ConditionalOnProperty(prefix = "spring.flyway", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public Map<String, MigrateResult> embeddedDatabaseMigrations(DataSource dataSource) {
        Map<String, MigrateResult> results = new LinkedHashMap<>();
        for (String module : MODULES) {
            MigrateResult result = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration/" + module)
                    .table("flyway_" + module + "_history")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .validateMigrationNaming(true)
                    .load()
                    .migrate();
            results.put(module, result);
            log.info("Embedded database migration completed: module={}, migrations={}",
                    module, result.migrationsExecuted);
        }
        return results;
    }
}
