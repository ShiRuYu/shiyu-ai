package com.shiyu.ai.governance.implementation.usage.port.repository;

import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Map;

/**
 * UsageRecordRepository 仓储接口，负责访问和持久化治理领域聚合数据。
 */
public interface UsageRecordRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param record 方法参数。
     */
    void insert(UsageRecordBO record);

    /**
     * 保存条件不存在。
     *
     * @param record record 参数。
     *
     * @return 判断结果。
     */
    default boolean insertIfAbsent(UsageRecordBO record) {
        insert(record);
        return true;
    }

    /**
     * 统计符合条件的数据。
     *
     * @param days 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> aggregateByDay(int days);

    /**
     * 统计符合条件的数据。
     *
     * @param weeks 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> aggregateByWeek(int weeks);

    /**
     * 统计符合条件的数据。
     *
     * @param months 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> aggregateByMonth(int months);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 操作结果。
     */
    Map<String, Object> getOverview();

    /**
     * 统计符合条件的数据。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> aggregateByModel();

    /**
     * 统计符合条件的数据。
     *
     * @param days 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> aggregateLlmByDay(int days);

    /**
     * 统计符合条件的数据。
     *
     * @param weeks 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> aggregateLlmByWeek(int weeks);

    /**
     * 统计符合条件的数据。
     *
     * @param months 方法参数。
     *
     * @return 操作结果。
     */
    List<Map<String, Object>> aggregateLlmByMonth(int months);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 操作结果。
     */
    Map<String, Object> getEmbeddingOverview();

    /**
     * 统计符合条件的数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    Long sumLlmTodayTokensByTenantId(TenantId tenantId);
}
