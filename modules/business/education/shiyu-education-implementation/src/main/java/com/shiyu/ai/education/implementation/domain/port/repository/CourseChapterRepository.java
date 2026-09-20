package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.CourseChapterBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 课程 章节 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface CourseChapterRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param courseId 课程标识。
     *
     * @return 符合条件的结果集合。
     */
    List<CourseChapterBO> selectByCourseId(TenantId tenantId, Long courseId);
}
