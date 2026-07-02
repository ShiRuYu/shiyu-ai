package com.shiyu.ai.education.course;

import com.shiyu.ai.dal.dataobject.education.CourseDO;
import com.shiyu.ai.education.dto.CourseProgressResponse;

import java.util.List;

public interface CourseService {

    CourseDO getById(Long id);

    List<CourseDO> listBySubjectCode(String subjectCode);

    List<CourseDO> listByGrade(Integer grade);

    List<CourseDO> listAll();

    CourseDO create(CourseDO course);

    /** 获取课程学习进度 */
    CourseProgressResponse getProgress(Long courseId, Long studentId);

    void update(CourseDO course);

    void deleteById(Long id);
}
