package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.CourseSectionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

public interface CourseSectionRepository {
    List<CourseSectionBO> selectByChapterIds(TenantId tenantId, List<Long> chapterIds);
}
