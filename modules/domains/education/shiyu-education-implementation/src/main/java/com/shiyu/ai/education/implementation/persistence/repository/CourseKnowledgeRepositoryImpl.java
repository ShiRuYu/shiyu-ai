package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.CourseKnowledgeBO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseKnowledgeMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseKnowledgeRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository
                .CourseKnowledgeRepository {

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
