package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.CourseKnowledgeBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.CourseKnowledgeDO;
import com.shiyu.ai.education.implementation.persistence.mapper.CourseKnowledgeMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CourseKnowledgeRepositoryImpl implements com.shiyu.ai.education.port.repository.CourseKnowledgeRepository {

    @Resource
    private CourseKnowledgeMapper courseKnowledgeMapper;

    public List<CourseKnowledgeBO> selectByCourseId(TenantId tenantId, Long courseId) {
        return MapstructUtils.convert(courseKnowledgeMapper.selectListByQuery(
                QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("course_id", courseId)), CourseKnowledgeBO.class);
    }
}

