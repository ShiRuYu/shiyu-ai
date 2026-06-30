package com.shiyu.ai.education.subject;

import com.shiyu.ai.dal.dataobject.education.SubjectDO;

import java.util.List;

public interface SubjectService {

    SubjectDO getById(Long id);

    SubjectDO getByCode(String code);

    List<SubjectDO> listAll();

    List<SubjectDO> listByGradeLevel(String gradeLevel);

    SubjectDO create(SubjectDO subject);

    void update(SubjectDO subject);

    void deleteById(Long id);
}
