package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.time.LocalDate;
import java.util.List;

public interface ReviewTaskRepository {
    ReviewTaskBO selectById(TenantId tenantId, Long id);
    List<ReviewTaskBO> selectTodayTasks(TenantId tenantId, Long studentId);
    List<ReviewTaskBO> selectByStudentAndStatus(TenantId tenantId, Long studentId, Integer status);
    List<ReviewTaskBO> selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId);
    int insert(TenantId tenantId, ReviewTaskBO entity);
    int update(TenantId tenantId, ReviewTaskBO entity);
    int deleteById(TenantId tenantId, Long id);
}
