package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudyPlanItemBO;
import java.time.LocalDate;
import java.util.List;

public interface StudyPlanItemRepository {
    List<StudyPlanItemBO> selectByPlanId(Long planId);
    List<StudyPlanItemBO> selectTodayItems(List<Long> planIds);
    int insertBatch(List<StudyPlanItemBO> items);
}
