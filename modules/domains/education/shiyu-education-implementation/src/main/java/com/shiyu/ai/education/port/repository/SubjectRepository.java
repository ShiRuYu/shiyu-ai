package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.SubjectBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface SubjectRepository {
    SubjectBO selectById(TenantId tenantId, Long id);
    SubjectBO selectByCode(TenantId tenantId, String code);
    PageData<SubjectBO> selectPage(TenantId tenantId, int pageNum, int pageSize);
    List<SubjectBO> selectByGradeLevel(TenantId tenantId, String gradeLevel);
    List<SubjectBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, SubjectBO entity);
    int update(TenantId tenantId, SubjectBO entity);
    int deleteById(TenantId tenantId, Long id);
}
