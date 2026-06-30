package com.shiyu.ai.education.exam;

import com.shiyu.ai.dal.dataobject.education.ExamDO;

import java.util.List;

public interface ExamService {

    ExamDO getById(Long id);

    List<ExamDO> listBySubjectCode(String subjectCode);

    List<ExamDO> listByTeacherId(Long teacherId);

    ExamDO create(ExamDO exam);

    void update(ExamDO exam);

    void deleteById(Long id);
}
