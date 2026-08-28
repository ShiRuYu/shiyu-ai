package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ExamBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface ExamRepository {
    ExamBO selectById(TenantId tenantId, Long id);
    PageData<ExamBO> selectPage(TenantId tenantId, int pageNum, int pageSize);
    List<ExamBO> selectBySubjectCode(TenantId tenantId, String subjectCode);
    List<ExamBO> selectByTeacherId(TenantId tenantId, Long teacherId);
    List<ExamBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, ExamBO entity);
    int update(TenantId tenantId, ExamBO entity);
    int deleteById(TenantId tenantId, Long id);
}
