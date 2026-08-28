package com.shiyu.ai.plugin.market.persistence;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.plugin.market.PluginMarketEntry;
import com.shiyu.ai.plugin.market.PluginMarketStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JdbcPluginMarketStore implements PluginMarketStore {
    private final JdbcTemplate jdbc;
    public JdbcPluginMarketStore(@Qualifier("agentDataSource") DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }

    @Override public PluginMarketEntry save(PluginMarketEntry entry) {
        jdbc.update("INSERT INTO PLUGIN_MARKET_ENTRY (ID,VERSION,SOURCE,MANIFEST,SIGNATURE,PUBLISHER_KEY,PERMISSIONS_JSON,CHECKSUM,UPDATE_POLICY,PUBLISHED_AT,ENABLED) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                entry.id(), entry.version(), entry.source(), entry.manifest(), entry.signature(), entry.publisherKey(), JSONUtils.toJsonString(entry.permissions()), entry.checksum(), entry.updatePolicy(), Timestamp.from(entry.publishedAt()), entry.enabled());
        return entry;
    }
    @Override public List<PluginMarketEntry> list() { return jdbc.query("SELECT * FROM PLUGIN_MARKET_ENTRY ORDER BY ID, PUBLISHED_AT DESC", (r,n) -> map(r)); }
    @Override public Optional<PluginMarketEntry> find(String id) { return jdbc.query("SELECT * FROM PLUGIN_MARKET_ENTRY WHERE ID=? ORDER BY PUBLISHED_AT DESC", (r,n) -> map(r), id).stream().findFirst(); }
    @Override public void disable(String id) { jdbc.update("UPDATE PLUGIN_MARKET_ENTRY SET ENABLED=FALSE WHERE ID=?", id); }

    @SuppressWarnings("unchecked")
    private PluginMarketEntry map(java.sql.ResultSet r) throws java.sql.SQLException {
        List<String> permissions = JSONUtils.parseObject(r.getString("PERMISSIONS_JSON"), List.class);
        Timestamp published = r.getTimestamp("PUBLISHED_AT");
        return new PluginMarketEntry(r.getString("ID"), r.getString("VERSION"), r.getString("SOURCE"), r.getString("MANIFEST"),
                r.getString("SIGNATURE"), r.getString("PUBLISHER_KEY"), permissions == null ? List.of() : permissions,
                r.getString("CHECKSUM"), r.getString("UPDATE_POLICY"), published == null ? Instant.EPOCH : published.toInstant(), r.getBoolean("ENABLED"));
    }
}

