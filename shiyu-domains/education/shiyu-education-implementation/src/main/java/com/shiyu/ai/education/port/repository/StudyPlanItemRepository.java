package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudyPlanItemBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.time.LocalDate;
import java.util.List;

public interface StudyPlanItemRepository {
    List<StudyPlanItemBO> selectByPlanId(TenantId tenantId, Long planId);
    List<StudyPlanItemBO> selectTodayItems(TenantId tenantId, List<Long> planIds);
    int insertBatch(TenantId tenantId, List<StudyPlanItemBO> items);
}
