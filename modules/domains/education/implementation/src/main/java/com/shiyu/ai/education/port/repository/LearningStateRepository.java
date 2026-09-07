package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.LearningStateBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface LearningStateRepository {
    LearningStateBO selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId);
    int upsert(TenantId tenantId, LearningStateBO state);
}
