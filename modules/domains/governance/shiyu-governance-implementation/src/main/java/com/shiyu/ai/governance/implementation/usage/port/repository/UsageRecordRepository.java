package com.shiyu.ai.governance.implementation.usage.port.repository;

import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Map;

/**
 * 负责 用量 Record 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface UsageRecordRepository {
    /**
     * 创建或保存 用量 Record 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param record 用于完成本次业务处理的 record 参数。
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
     * 执行 用量 Record 相关业务数据，并返回处理结果。
     *
     * @param days 用于完成本次业务处理的 days 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Map<String, Object>> aggregateByDay(int days);

    /**
     * 执行 用量 Record 相关业务数据，并返回处理结果。
     *
     * @param weeks 用于完成本次业务处理的 weeks 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Map<String, Object>> aggregateByWeek(int weeks);

    /**
     * 执行 用量 Record 相关业务数据，并返回处理结果。
     *
     * @param months 用于完成本次业务处理的 months 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Map<String, Object>> aggregateByMonth(int months);

    /**
     * 查询 用量 Record 相关业务数据，并返回处理结果。
     *
     * @return 返回 用量 Record 相关操作生成的结果数据。
     */
    Map<String, Object> getOverview();

    /**
     * 执行 用量 Record 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Map<String, Object>> aggregateByModel();

    /**
     * 执行 用量 Record 相关业务数据，并返回处理结果。
     *
     * @param days 用于完成本次业务处理的 days 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Map<String, Object>> aggregateLlmByDay(int days);

    /**
     * 执行 用量 Record 相关业务数据，并返回处理结果。
     *
     * @param weeks 用于完成本次业务处理的 weeks 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Map<String, Object>> aggregateLlmByWeek(int weeks);

    /**
     * 执行 用量 Record 相关业务数据，并返回处理结果。
     *
     * @param months 用于完成本次业务处理的 months 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Map<String, Object>> aggregateLlmByMonth(int months);

    /**
     * 查询 用量 Record 相关业务数据，并返回处理结果。
     *
     * @return 返回 用量 Record 相关操作生成的结果数据。
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
