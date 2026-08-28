package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudyPlanBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface StudyPlanRepository {
    StudyPlanBO selectById(TenantId tenantId, Long id);
    List<StudyPlanBO> selectByStudentId(TenantId tenantId, Long studentId);
    List<StudyPlanBO> selectActiveByStudent(TenantId tenantId, Long studentId);
    int insert(TenantId tenantId, StudyPlanBO entity);
    int update(TenantId tenantId, StudyPlanBO entity);
    int deleteById(TenantId tenantId, Long id);
}
