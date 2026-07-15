package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.bo.education.CourseBO;
import com.shiyu.ai.education.dto.CourseProgressResponse;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.CourseResponse;
import com.shiyu.ai.education.request.CourseRequest;

/**
 * Course 接口
 */

public interface CourseService {

    /**
     * Get By Id
     * @return 处理结果
     */
    CourseResponse getById(Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<CourseResponse> listBySubjectCode(String subjectCode);

    /**
     * List By Grade
     * @return 处理结果
     */
    List<CourseResponse> listByGrade(Integer grade);

    /**
     * List All
     * @return 处理结果
     */

    PageData<CourseResponse> page(int pageNum, int pageSize);

    /**
     * Create
     * @param CourseResponse CourseDO
     * @return 处理结果
     */
    CourseResponse create(CourseRequest course);

    /** 获取课程学习进度 */
    CourseProgressResponse getProgress(Long courseId, Long studentId);

    void update(CourseRequest course);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
    /**
     * Record study session
     */
    void recordStudy(com.shiyu.ai.dal.bo.education.StudyRecordBO record);
}