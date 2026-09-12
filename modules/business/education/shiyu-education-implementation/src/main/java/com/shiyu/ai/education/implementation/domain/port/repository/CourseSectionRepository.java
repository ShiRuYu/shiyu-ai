package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.CourseSectionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * CourseSectionRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface CourseSectionRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param chapterIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<CourseSectionBO> selectByChapterIds(TenantId tenantId, List<Long> chapterIds);
}
