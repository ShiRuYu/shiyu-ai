package com.shiyu.ai.tooling.implementation.plugin.market.persistence;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.tooling.implementation.plugin.market.model.PluginMarketEntry;
import com.shiyu.ai.tooling.implementation.plugin.market.port.PluginMarketStore;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * {@code JdbcPluginMarketStore} 承载工具模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
public class JdbcPluginMarketStore implements PluginMarketStore {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * {@code JdbcPluginMarketStore} 创建并初始化当前类型实例。
     *
     * @param dataSource 参数值，用于执行当前操作。
     */
    public JdbcPluginMarketStore(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * {@code save} 写入或更新当前模块中的业务数据。
     *
     * @param entry 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PluginMarketEntry save(PluginMarketEntry entry) {
        jdbc.update(
                "INSERT INTO PLUGIN_MARKET_ENTRY"
                    + " (ID,VERSION,SOURCE,MANIFEST,SIGNATURE,PUBLISHER_KEY,PERMISSIONS_JSON,CHECKSUM,UPDATE_POLICY,PUBLISHED_AT,ENABLED)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                entry.id(),
                entry.version(),
                entry.source(),
                entry.manifest(),
                entry.signature(),
                entry.publisherKey(),
                JSONUtils.toJsonString(entry.permissions()),
                entry.checksum(),
                entry.updatePolicy(),
                Timestamp.from(entry.publishedAt()),
                entry.enabled());
        return entry;
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<PluginMarketEntry> list() {
        return jdbc.query(
                "SELECT * FROM PLUGIN_MARKET_ENTRY ORDER BY ID, PUBLISHED_AT DESC",
                (r, n) -> map(r));
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<PluginMarketEntry> find(String id) {
        return jdbc
                .query(
                        "SELECT * FROM PLUGIN_MARKET_ENTRY WHERE ID=? ORDER BY PUBLISHED_AT DESC",
                        (r, n) -> map(r),
                        id)
                .stream()
                .findFirst();
    }

    /**
     * {@code disable} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void disable(String id) {
        jdbc.update("UPDATE PLUGIN_MARKET_ENTRY SET ENABLED=FALSE WHERE ID=?", id);
    }

    @SuppressWarnings("unchecked")
    private PluginMarketEntry map(java.sql.ResultSet r) throws java.sql.SQLException {
        List<String> permissions =
                JSONUtils.parseObject(r.getString("PERMISSIONS_JSON"), List.class);
        Timestamp published = r.getTimestamp("PUBLISHED_AT");
        return new PluginMarketEntry(
                r.getString("ID"),
                r.getString("VERSION"),
                r.getString("SOURCE"),
                r.getString("MANIFEST"),
                r.getString("SIGNATURE"),
                r.getString("PUBLISHER_KEY"),
                permissions == null ? List.of() : permissions,
                r.getString("CHECKSUM"),
                r.getString("UPDATE_POLICY"),
                published == null ? Instant.EPOCH : published.toInstant(),
                r.getBoolean("ENABLED"));
    }
}
