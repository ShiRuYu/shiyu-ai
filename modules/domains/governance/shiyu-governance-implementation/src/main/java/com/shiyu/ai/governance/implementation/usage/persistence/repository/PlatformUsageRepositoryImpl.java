package com.shiyu.ai.governance.implementation.usage.persistence.repository;

import com.shiyu.ai.common.core.jdbc.JdbcDialect;
import com.shiyu.ai.governance.implementation.usage.persistence.mapper.UsageRecordMapper;
import com.shiyu.ai.governance.implementation.usage.port.repository.PlatformUsageRepository;
import com.shiyu.ai.model.contract.api.ModelCatalogPort;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 负责 平台 用量 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public final class PlatformUsageRepositoryImpl
        implements PlatformUsageRepository {

    private final UsageRecordRepositoryImpl queries;

    /**
     * 创建平台统计仓储。
     *
     * @param usageRecordMapper 用量记录映射器。
     * @param modelCatalog 模型目录。
     * @param dataSource 用量数据库数据源。
     */
    @Autowired
    public PlatformUsageRepositoryImpl(
            UsageRecordMapper usageRecordMapper,
            ModelCatalogPort modelCatalog,
            @Qualifier("agentDataSource") DataSource dataSource) {
        queries = new UsageRecordRepositoryImpl(
                usageRecordMapper,
                modelCatalog,
                JdbcDialect.detect(new JdbcTemplate(dataSource)),
                true);
    }

    @Override
    public Map<String, Object> getOverview() { return queries.getOverview(); }
    @Override
    public List<Map<String, Object>> aggregateByDay(int days) { return queries.aggregateByDay(days); }
    @Override
    public List<Map<String, Object>> aggregateByWeek(int weeks) { return queries.aggregateByWeek(weeks); }
    @Override
    public List<Map<String, Object>> aggregateByMonth(int months) { return queries.aggregateByMonth(months); }
    @Override
    public List<Map<String, Object>> aggregateByModel() { return queries.aggregateByModel(); }
    @Override
    public List<Map<String, Object>> aggregateLlmByDay(int days) { return queries.aggregateLlmByDay(days); }
    @Override
    public List<Map<String, Object>> aggregateLlmByWeek(int weeks) { return queries.aggregateLlmByWeek(weeks); }
    @Override
    public List<Map<String, Object>> aggregateLlmByMonth(int months) { return queries.aggregateLlmByMonth(months); }
    @Override
    public Map<String, Object> getEmbeddingOverview() { return queries.getEmbeddingOverview(); }
}
