package com.shiyu.ai.education.student;

import com.shiyu.ai.dal.dataobject.education.StudentDO;

public interface StudentService {

    StudentDO getById(Long id);

    StudentDO getByUserId(Long userId);

    StudentDO create(StudentDO student);

    void update(StudentDO student);
}
