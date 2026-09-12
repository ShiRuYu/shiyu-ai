package com.shiyu.ai.agent.implementation.persistence.runtime;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.port.AiAppRepository;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * {@code JdbcAiAppRepository} 定义智能体模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
@Repository
@Primary
public class JdbcAiAppRepository implements AiAppRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * {@code JdbcAiAppRepository} 创建并初始化当前类型实例。
     *
     * @param jdbc 参数值，用于执行当前操作。
     */
    public JdbcAiAppRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param a 参数值，用于执行当前操作。
     */
    @Override
    public void insert(AiApp a) {
        jdbc.update(
                "INSERT INTO AI_APP"
                    + " (ID,TENANT_ID,OWNER_USER_ID,NAME,DESCRIPTION,STATUS,PUBLISHED_VERSION_ID,CREATED_AT,UPDATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?)",
                a.id(),
                a.tenantId().value(),
                a.ownerUserId().value(),
                a.name(),
                a.description(),
                a.status(),
                a.publishedVersionId(),
                Timestamp.from(a.createdAt()),
                Timestamp.from(a.updatedAt()));
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiApp> find(String id, TenantId tenant, long owner) {
        return jdbc.query(
                "SELECT * FROM AI_APP WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=?",
                rs -> rs.next() ? Optional.of(mapApp(rs)) : Optional.empty(),
                id,
                tenant(tenant),
                owner);
    }

    /**
     * {@code findByTenant} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenant 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiApp> findByTenant(String id, TenantId tenant) {
        return jdbc.query(
                "SELECT * FROM AI_APP WHERE ID=? AND TENANT_ID=?",
                rs -> rs.next() ? Optional.of(mapApp(rs)) : Optional.empty(),
                id,
                tenant(tenant));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiApp> list(TenantId tenant, long owner, int limit) {
        return jdbc.query(
                "SELECT * FROM AI_APP WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY UPDATED_AT"
                        + " DESC,ID ASC LIMIT ?",
                (rs, n) -> mapApp(rs),
                tenant(tenant),
                owner,
                Math.max(1, Math.min(limit, 100)));
    }

    /**
     * {@code insertVersion} 执行当前类型定义的业务操作。
     *
     * @param v 参数值，用于执行当前操作。
     */
    @Override
    public void insertVersion(AiAppVersion v) {
        jdbc.update(
                "INSERT INTO AI_APP_VERSION"
                    + " (ID,APP_ID,TENANT_ID,VERSION,CONFIG_JSON,STATUS,CREATED_AT,PUBLISHED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?)",
                v.id(),
                v.appId(),
                v.tenantId().value(),
                v.version(),
                v.configJson(),
                v.status(),
                Timestamp.from(v.createdAt()),
                v.publishedAt() == null ? null : Timestamp.from(v.publishedAt()));
    }

    /**
     * {@code findVersion} 查询并返回当前操作所需的数据。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param tenant 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiAppVersion> findVersion(String appId, String id, TenantId tenant) {
        return jdbc.query(
                "SELECT * FROM AI_APP_VERSION WHERE APP_ID=? AND ID=? AND TENANT_ID=?",
                rs -> rs.next() ? Optional.of(mapVersion(rs)) : Optional.empty(),
                appId,
                id,
                tenant(tenant));
    }

    /**
     * {@code versions} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param tenant 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiAppVersion> versions(String appId, TenantId tenant) {
        return jdbc.query(
                "SELECT * FROM AI_APP_VERSION WHERE APP_ID=? AND TENANT_ID=? ORDER BY CREATED_AT"
                        + " DESC,ID ASC",
                (rs, n) -> mapVersion(rs),
                appId,
                tenant(tenant));
    }

    /**
     * {@code publishVersion} 执行当前模块定义的业务流程。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param tenant 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional
    public int publishVersion(String appId, String id, TenantId tenant) {
        long tenantValue = tenant(tenant);
        int n =
                jdbc.update(
                        "UPDATE AI_APP_VERSION SET"
                            + " STATUS='PUBLISHED',PUBLISHED_AT=CURRENT_TIMESTAMP WHERE APP_ID=?"
                            + " AND ID=? AND TENANT_ID=? AND STATUS='DRAFT'",
                        appId,
                        id,
                        tenantValue);
        if (n == 1)
            jdbc.update(
                    "UPDATE AI_APP_VERSION SET STATUS='ARCHIVED' WHERE APP_ID=? AND TENANT_ID=? AND"
                            + " STATUS='PUBLISHED' AND ID<>?",
                    appId,
                    tenantValue,
                    id);
        if (n == 1)
            jdbc.update(
                    "UPDATE AI_APP SET PUBLISHED_VERSION_ID=?,UPDATED_AT=CURRENT_TIMESTAMP WHERE"
                            + " ID=? AND TENANT_ID=?",
                    id,
                    appId,
                    tenantValue);
        return n;
    }

    /**
     * {@code archiveVersion} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param tenant 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int archiveVersion(String appId, String id, TenantId tenant) {
        return jdbc.update(
                "UPDATE AI_APP_VERSION SET STATUS='ARCHIVED' WHERE APP_ID=? AND ID=? AND"
                        + " TENANT_ID=? AND STATUS='DRAFT'",
                appId,
                id,
                tenant(tenant));
    }

    private AiApp mapApp(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AiApp(
                rs.getString("ID"),
                new TenantId(rs.getLong("TENANT_ID")),
                new UserId(rs.getLong("OWNER_USER_ID")),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("STATUS"),
                rs.getString("PUBLISHED_VERSION_ID"),
                rs.getTimestamp("CREATED_AT").toInstant(),
                rs.getTimestamp("UPDATED_AT").toInstant());
    }

    private AiAppVersion mapVersion(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AiAppVersion(
                rs.getString("ID"),
                rs.getString("APP_ID"),
                new TenantId(rs.getLong("TENANT_ID")),
                rs.getString("VERSION"),
                rs.getString("CONFIG_JSON"),
                rs.getString("STATUS"),
                rs.getTimestamp("CREATED_AT").toInstant(),
                rs.getTimestamp("PUBLISHED_AT") == null
                        ? null
                        : rs.getTimestamp("PUBLISHED_AT").toInstant());
    }

    private static long tenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }
}
