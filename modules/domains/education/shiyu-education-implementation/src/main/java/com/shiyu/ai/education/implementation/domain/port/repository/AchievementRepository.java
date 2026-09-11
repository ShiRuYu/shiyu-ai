package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.AchievementBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

public interface AchievementRepository {
    List<AchievementBO> selectByStudent(TenantId tenantId, Long studentId);

    int insert(TenantId tenantId, AchievementBO a);
}
