package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.QuestionBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface QuestionRepository {
    QuestionBO selectById(TenantId tenantId, Long id);
    PageData<QuestionBO> selectPage(TenantId tenantId, int pageNum, int pageSize);
    List<QuestionBO> selectBySubjectAndGrade(TenantId tenantId, String subjectCode, Integer grade);
    List<QuestionBO> selectByDifficulty(TenantId tenantId, Integer difficulty);
    List<QuestionBO> selectByType(TenantId tenantId, String type);
    QuestionBO selectByCode(TenantId tenantId, String code);
    void incrementUsedCount(TenantId tenantId, Long id);
    List<QuestionBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, QuestionBO entity);
    int update(TenantId tenantId, QuestionBO entity);
    int deleteById(TenantId tenantId, Long id);
}
