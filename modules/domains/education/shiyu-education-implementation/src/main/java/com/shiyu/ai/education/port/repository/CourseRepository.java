package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.CourseBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface CourseRepository {
    CourseBO selectById(TenantId tenantId, Long id);
    PageData<CourseBO> selectPage(TenantId tenantId, int pageNum, int pageSize);
    List<CourseBO> selectBySubjectCode(TenantId tenantId, String subjectCode);
    List<CourseBO> selectByGrade(TenantId tenantId, Integer grade);
    List<CourseBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, CourseBO entity);
    int update(TenantId tenantId, CourseBO entity);
    int deleteById(TenantId tenantId, Long id);
}
