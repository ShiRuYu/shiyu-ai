package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.StudyPlanItemBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * StudyPlanItemRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface StudyPlanItemRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param planId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<StudyPlanItemBO> selectByPlanId(TenantId tenantId, Long planId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param planIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<StudyPlanItemBO> selectTodayItems(TenantId tenantId, List<Long> planIds);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param items 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insertBatch(TenantId tenantId, List<StudyPlanItemBO> items);
}
