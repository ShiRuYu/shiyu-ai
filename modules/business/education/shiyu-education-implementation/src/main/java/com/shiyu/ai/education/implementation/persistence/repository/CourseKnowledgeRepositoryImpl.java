package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.CourseKnowledgeBO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseKnowledgeMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 课程 知识 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class CourseKnowledgeRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .CourseKnowledgeRepository {

    /**
     * courseKnowledgeMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private CourseKnowledgeMapper courseKnowledgeMapper;

    public List<CourseKnowledgeBO> selectByCourseId(TenantId tenantId, Long courseId) {
        return MapstructUtils.convert(
                courseKnowledgeMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("course_id", courseId)),
                CourseKnowledgeBO.class);
    }
}
