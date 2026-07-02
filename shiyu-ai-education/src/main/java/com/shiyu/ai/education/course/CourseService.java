package com.shiyu.ai.education.course;

import com.shiyu.ai.dal.dataobject.education.CourseDO;
import com.shiyu.ai.education.dto.CourseProgressResponse;

import java.util.List;

/**
 * Course 接口
 */

public interface CourseService {

    /**
     * Get By Id
     * @return 处理结果
     */
    CourseDO getById(Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<CourseDO> listBySubjectCode(String subjectCode);

    /**
     * List By Grade
     * @return 处理结果
     */
    List<CourseDO> listByGrade(Integer grade);

    /**
     * List All
     * @return 处理结果
     */
    List<CourseDO> listAll();

    /**
     * Create
     * @param CourseDO CourseDO
     * @return 处理结果
     */
    CourseDO create(CourseDO course);

    /** 获取课程学习进度 */
    CourseProgressResponse getProgress(Long courseId, Long studentId);

    void update(CourseDO course);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
