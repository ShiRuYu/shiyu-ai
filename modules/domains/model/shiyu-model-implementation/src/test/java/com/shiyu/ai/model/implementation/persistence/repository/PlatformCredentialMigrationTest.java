package com.shiyu.ai.model.implementation.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/** 验证旧平台凭证在应用启动前迁移，且不接受错误主密钥。 */
class PlatformCredentialMigrationTest {
    private static final String MASTER_KEY = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    @Test
    void migratesLegacyPlaintextOnceAndVerifiesExistingCiphertext() {
        JdbcDataSource dataSource = database();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE model_ai_platform "
                + "(id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, api_key VARCHAR(500))");
        PlatformCredentialCipher cipher = new PlatformCredentialCipher(MASTER_KEY);
        jdbc.update("INSERT INTO model_ai_platform VALUES (1,7,?)", "legacy-test-key");
        jdbc.update("INSERT INTO model_ai_platform VALUES (2,8,?)",
                cipher.protect(8L, "already-protected"));

        PlatformCredentialMigration migration = new PlatformCredentialMigration(dataSource, cipher);
        assertThat(migration.migrate()).isEqualTo(1);
        String stored = jdbc.queryForObject(
                "SELECT api_key FROM model_ai_platform WHERE id = 1", String.class);
        assertThat(stored).startsWith("enc:v1:").doesNotContain("legacy-test-key");
        assertThat(cipher.unprotect(7L, stored)).isEqualTo("legacy-test-key");
        assertThat(migration.migrate()).isZero();
    }

    @Test
    void rejectsExistingCredentialsWhenMasterKeyIsUnavailable() {
        JdbcDataSource dataSource = database();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE model_ai_platform "
                + "(id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, api_key VARCHAR(500))");
        jdbc.update("INSERT INTO model_ai_platform VALUES (1,7,?)", "legacy-test-key");

        assertThatThrownBy(() -> new PlatformCredentialMigration(
                dataSource, new PlatformCredentialCipher("")).migrate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsExistingCiphertextWhenMasterKeyHasChanged() {
        JdbcDataSource dataSource = database();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE model_ai_platform "
                + "(id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, api_key VARCHAR(500))");
        jdbc.update("INSERT INTO model_ai_platform VALUES (1,7,?)",
                new PlatformCredentialCipher(MASTER_KEY).protect(7L, "temporary-test-key"));
        String wrongKey = Base64.getEncoder().encodeToString(
                "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> new PlatformCredentialMigration(
                dataSource, new PlatformCredentialCipher(wrongKey)).migrate())
                .isInstanceOf(IllegalStateException.class);
    }

    private static JdbcDataSource database() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:platform-credentials-" + UUID.randomUUID()
                + ";DB_CLOSE_DELAY=-1");
        return source;
    }
}
