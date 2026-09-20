package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.StudyPlanItemBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 Study Plan Item 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface StudyPlanItemRepository {
    /**
     * 查询 Study Plan Item 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param planId 用于定位plan的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<StudyPlanItemBO> selectByPlanId(TenantId tenantId, Long planId);

    /**
     * 查询 Study Plan Item 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param planIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<StudyPlanItemBO> selectTodayItems(TenantId tenantId, List<Long> planIds);

    /**
     * 创建或保存 Study Plan Item 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param items 用于完成本次业务处理的 items 参数。
     * @return 返回 Study Plan Item 相关操作生成的结果数据。
     */
    int insertBatch(TenantId tenantId, List<StudyPlanItemBO> items);
}
