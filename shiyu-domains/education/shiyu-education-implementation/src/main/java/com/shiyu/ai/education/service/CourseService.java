package com.shiyu.ai.education.service;

import com.shiyu.ai.education.dto.CourseProgressResponse;

import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.CourseResponse;
import com.shiyu.ai.education.request.CourseRequest;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Course 接口
 */

public interface CourseService {

    /**
     * Get By Id
     * @return 处理结果
     */
    CourseResponse getById(ActorContext actor, Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<CourseResponse> listBySubjectCode(ActorContext actor, String subjectCode);

    /**
     * List By Grade
     * @return 处理结果
     */
    List<CourseResponse> listByGrade(ActorContext actor, Integer grade);

    /**
     * List All
     * @return 处理结果
     */

    PageData<CourseResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * Create
     * @param CourseResponse CourseDO
     * @return 处理结果
     */
    CourseResponse create(ActorContext actor, CourseRequest course);

    /** 获取课程学习进度 */
    CourseProgressResponse getProgress(ActorContext actor, Long courseId, Long studentId);

    void update(ActorContext actor, CourseRequest course);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(ActorContext actor, Long id);
}
