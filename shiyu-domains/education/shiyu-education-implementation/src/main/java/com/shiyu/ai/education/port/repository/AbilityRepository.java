package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface AbilityRepository {
    AbilityBO selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId);
    List<AbilityBO> selectByStudent(TenantId tenantId, Long studentId);
    int insert(TenantId tenantId, AbilityBO ability);
    int update(TenantId tenantId, AbilityBO ability);
}
