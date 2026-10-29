package com.shiyu.ai.model.implementation.persistence.repository;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelRoutePolicy;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 持久保存租户路由策略，在数据库基线初始化后安装增补表。 */
@Repository
public class ModelRoutePolicyStore implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    /**
     * 创建使用业务数据源的路由策略仓储。
     * @param dataSource 业务数据库
     */
    public ModelRoutePolicyStore(@Qualifier("agentDataSource") DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
    }
    @Override
    public void run(ApplicationArguments args) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS model_route_policy (id VARCHAR(100) PRIMARY KEY, tenant_id BIGINT NOT NULL, payload VARCHAR(10000) NOT NULL)");
    }
    /**
     * 保存路由，成功返回之前完成数据库写入。
     * @param policy 路由策略
     */
    public void save(ModelRoutePolicy policy) {
        String payload = JSONUtils.toJsonString(policy);
        if (jdbc.update("UPDATE model_route_policy SET payload = ? WHERE id = ? AND tenant_id = ?",
                payload, policy.id(), policy.tenantId()) == 0) {
            jdbc.update("INSERT INTO model_route_policy (id, tenant_id, payload) VALUES (?, ?, ?)",
                    policy.id(), policy.tenantId(), payload);
        }
    }
    /**
     * 查询指定租户的策略。
     * @param tenantId 租户标识
     * @return 租户路由集合
     */
    public List<ModelRoutePolicy> list(long tenantId) {
        return jdbc.query("SELECT payload FROM model_route_policy WHERE tenant_id = ? ORDER BY id",
                (row, index) -> JSONUtils.parseObject(row.getString(1), ModelRoutePolicy.class), tenantId);
    }
    /**
     * 按租户及标识读取单条路由，避免加载全部租户策略。
     * @param tenantId 租户标识
     * @param id 路由标识
     * @return 匹配的路由策略
     */
    public java.util.Optional<ModelRoutePolicy> find(long tenantId, String id) {
        return jdbc.query("SELECT payload FROM model_route_policy WHERE tenant_id = ? AND id = ?",
                (row, index) -> JSONUtils.parseObject(row.getString(1), ModelRoutePolicy.class), tenantId, id)
                .stream().findFirst();
    }

}
