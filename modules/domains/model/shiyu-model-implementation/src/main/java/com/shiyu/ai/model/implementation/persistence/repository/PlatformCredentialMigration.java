package com.shiyu.ai.model.implementation.persistence.repository;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 在应用接受请求前迁移并校验数据库中的平台凭证。 */
@Component
public final class PlatformCredentialMigration implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PlatformCredentialCipher cipher;

    /**
     * 创建平台凭证迁移器。
     *
     * @param dataSource 平台业务数据库。
     * @param cipher 平台凭证加密器。
     */
    public PlatformCredentialMigration(
            @Qualifier("agentDataSource") DataSource dataSource,
            PlatformCredentialCipher cipher) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.cipher = cipher;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrate();
    }

    int migrate() {
        List<StoredCredential> rows = jdbc.query(
                "SELECT id, tenant_id, api_key FROM model_ai_platform "
                        + "WHERE api_key IS NOT NULL AND api_key <> ''",
                (result, rowNum) -> new StoredCredential(
                        result.getLong("id"), result.getLong("tenant_id"),
                        result.getString("api_key")));
        int migrated = 0;
        for (StoredCredential row : rows) {
            // 即使无需迁移，也要验证主密钥可以解开现有密文。
            String plaintext = cipher.unprotect(row.tenantId(), row.value());
            if (cipher.isProtected(row.value())) continue;
            String encrypted = cipher.protect(row.tenantId(), plaintext);
            int changed = jdbc.update(
                    "UPDATE model_ai_platform SET api_key = ? "
                            + "WHERE id = ? AND tenant_id = ? AND api_key = ?",
                    encrypted, row.id(), row.tenantId(), row.value());
            if (changed != 1) {
                throw new IllegalStateException("Platform credential changed during migration");
            }
            migrated++;
        }
        return migrated;
    }

    private record StoredCredential(long id, long tenantId, String value) {}
}
