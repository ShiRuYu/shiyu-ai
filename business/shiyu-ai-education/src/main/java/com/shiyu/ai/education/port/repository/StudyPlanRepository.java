package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudyPlanBO;
import java.util.List;

public interface StudyPlanRepository {
    StudyPlanBO selectById(Long id);
    List<StudyPlanBO> selectByStudentId(Long studentId);
    List<StudyPlanBO> selectActiveByStudent(Long studentId);
    int insert(StudyPlanBO entity);
    int update(StudyPlanBO entity);
    int deleteById(Long id);
}
