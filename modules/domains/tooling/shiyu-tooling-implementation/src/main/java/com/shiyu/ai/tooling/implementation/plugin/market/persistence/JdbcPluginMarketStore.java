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
 * 管理 Jdbc 插件 Market 相关的运行时状态、注册信息或临时数据。
 */
@Component
public class JdbcPluginMarketStore implements PluginMarketStore {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * 执行 Jdbc 插件 Market 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSource 用于完成本次业务处理的 dataSource 参数。
     */
    public JdbcPluginMarketStore(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * 创建或保存 Jdbc 插件 Market 相关业务数据，并返回处理结果。
     *
     * @param entry 用于完成本次业务处理的 entry 参数。
     * @return 返回 Jdbc 插件 Market 相关操作生成的结果数据。
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
     * 查询 Jdbc 插件 Market 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<PluginMarketEntry> list() {
        return jdbc.query(
                "SELECT * FROM PLUGIN_MARKET_ENTRY ORDER BY ID, PUBLISHED_AT DESC",
                (r, n) -> map(r));
    }

    /**
     * 查询 Jdbc 插件 Market 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 更新或设置 Jdbc 插件 Market 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
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
