package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.CourseProgressResponse;
import com.shiyu.ai.education.implementation.web.dto.CourseResponse;
import com.shiyu.ai.education.implementation.web.request.CourseRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 课程应用服务接口。
 *
 * <p>负责课程查询、创建、更新、删除及学习进度查询。</p>
 */
public interface CourseService {

    /**
     * 根据课程标识获取课程详情。
     *
     * @param actor 调用方上下文
     * @param id 课程标识
     * @return 课程详情
     */
    CourseResponse getById(ActorContext actor, Long id);

    /**
     * 按学科编码查询课程列表。
     *
     * @param actor 调用方上下文
     * @param subjectCode 学科编码
     * @return 课程列表
     */
    List<CourseResponse> listBySubjectCode(ActorContext actor, String subjectCode);

    /**
     * 按年级查询课程列表。
     *
     * @param actor 调用方上下文
     * @param grade 年级
     * @return 课程列表
     */
    List<CourseResponse> listByGrade(ActorContext actor, Integer grade);

    /**
     * 分页查询课程。
     *
     * @param actor 调用方上下文
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页课程结果
     */
    PageData<CourseResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 创建课程。
     *
     * @param actor 调用方上下文
     * @param course 课程创建请求
     * @return 创建后的课程
     */
    CourseResponse create(ActorContext actor, CourseRequest course);

    /**
     * 获取指定课程和学生的学习进度。
     *
     * @param actor 调用方上下文
     * @param courseId 课程标识
     * @param studentId 学生标识
     * @return 学习进度
     */
    CourseProgressResponse getProgress(ActorContext actor, Long courseId, Long studentId);

    /**
     * 更新课程信息。
     *
     * @param actor 调用方上下文
     * @param course 课程更新请求
     */
    void update(ActorContext actor, CourseRequest course);

    /**
     * 根据课程标识删除课程。
     *
     * @param actor 调用方上下文
     * @param id 课程标识
     */
    void deleteById(ActorContext actor, Long id);
}
