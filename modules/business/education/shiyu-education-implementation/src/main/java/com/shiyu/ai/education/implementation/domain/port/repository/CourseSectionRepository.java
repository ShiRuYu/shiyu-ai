package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.CourseSectionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 课程 Section 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface CourseSectionRepository {
    /**
     * 查询 课程 Section 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param chapterIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<CourseSectionBO> selectByChapterIds(TenantId tenantId, List<Long> chapterIds);
}
