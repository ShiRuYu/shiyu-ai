package com.shiyu.ai.agent.implementation.persistence.runtime;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.port.AiAppRepository;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 负责 Jdbc AI 应用 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Repository
@Primary
public class JdbcAiAppRepository implements AiAppRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * 执行 Jdbc AI 应用 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
     */
    public JdbcAiAppRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 创建或保存 Jdbc AI 应用 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param a 用于完成本次业务处理的 a 参数。
     */
    @Override
    public void insert(AiApp a) {
        long tenantValue = tenant(a.tenantId());
        jdbc.update(
                "INSERT INTO AI_APP"
                    + " (ID,TENANT_ID,OWNER_USER_ID,NAME,DESCRIPTION,STATUS,PUBLISHED_VERSION_ID,CREATED_AT,UPDATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?)",
                a.id(),
                tenantValue,
                a.ownerUserId().value(),
                a.name(),
                a.description(),
                a.status(),
                a.publishedVersionId(),
                Timestamp.from(a.createdAt()),
                Timestamp.from(a.updatedAt()));
    }

    /**
     * 查询 Jdbc AI 应用 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 Jdbc AI 应用 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenant 当前操作涉及的租户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 Jdbc AI 应用 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 创建或保存 Jdbc AI 应用 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param v 用于完成本次业务处理的 v 参数。
     */
    @Override
    public void insertVersion(AiAppVersion v) {
        long tenantValue = tenant(v.tenantId());
        jdbc.update(
                "INSERT INTO AI_APP_VERSION"
                    + " (ID,APP_ID,TENANT_ID,VERSION,CONFIG_JSON,STATUS,CREATED_AT,PUBLISHED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?)",
                v.id(),
                v.appId(),
                tenantValue,
                v.version(),
                v.configJson(),
                v.status(),
                Timestamp.from(v.createdAt()),
                v.publishedAt() == null ? null : Timestamp.from(v.publishedAt()));
    }

    /**
     * 查询 Jdbc AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param id 用于定位目标业务对象的标识。
     * @param tenant 当前操作涉及的租户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 执行 Jdbc AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param tenant 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 发布或发送 Jdbc AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param id 用于定位目标业务对象的标识。
     * @param tenant 当前操作涉及的租户标识。
     * @return 返回 Jdbc AI 应用 相关操作生成的结果数据。
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
     * 执行 Jdbc AI 应用 相关业务数据，并返回处理结果。
     *
     * @param appId 用于定位app的标识。
     * @param id 用于定位目标业务对象的标识。
     * @param tenant 当前操作涉及的租户标识。
     * @return 返回 Jdbc AI 应用 相关操作生成的结果数据。
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
        TenantScope.requireMatches(tenantId);
        return tenantId.value();
    }
}
