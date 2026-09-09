package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.CourseChapterBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface CourseChapterRepository {
    List<CourseChapterBO> selectByCourseId(TenantId tenantId, Long courseId);
}

