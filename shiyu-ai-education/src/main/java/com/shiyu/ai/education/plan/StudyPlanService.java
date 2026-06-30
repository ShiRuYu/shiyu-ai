package com.shiyu.ai.education.plan;

import com.shiyu.ai.dal.dataobject.education.StudyPlanDO;

import java.util.List;

public interface StudyPlanService {

    StudyPlanDO getById(Long id);

    List<StudyPlanDO> listByStudentId(Long studentId);

    List<StudyPlanDO> listActiveByStudent(Long studentId);

    StudyPlanDO create(StudyPlanDO plan);

    void update(StudyPlanDO plan);

    void deleteById(Long id);
}
