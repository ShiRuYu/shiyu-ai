package com.shiyu.ai.dal.knowledge;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class KnowledgeMigrationTest {

    @Test
    void enterpriseKnowledgeSchemaMigratesOnCleanH2() throws Exception {
        String url = "jdbc:h2:mem:knowledge_migration;MODE=MySQL;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations(
                        "classpath:db/migration/knowledge/ddl",
                        "classpath:db/migration/vector/ddl")
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var tables = connection.prepareStatement("""
                     SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
                     WHERE TABLE_NAME IN ('KNOWLEDGE_SPACE', 'KNOWLEDGE_DOCUMENT_VERSION',
                                          'KNOWLEDGE_INGESTION_JOB', 'KNOWLEDGE_AUDIT_LOG')
                     """).executeQuery()) {
            assertTrue(tables.next());
            assertEquals(4, tables.getInt(1));
        }

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var columns = connection.prepareStatement("""
                     SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                     WHERE TABLE_NAME = 'VECTOR_KNOWLEDGE_CHUNK'
                       AND COLUMN_NAME IN ('SPACE_ID', 'VERSION_ID', 'EMBEDDING_BINARY',
                                           'EMBEDDING_MODEL', 'EMBEDDING_DIMENSION')
                     """).executeQuery()) {
            assertTrue(columns.next());
            assertEquals(5, columns.getInt(1));
        }
    }
}
