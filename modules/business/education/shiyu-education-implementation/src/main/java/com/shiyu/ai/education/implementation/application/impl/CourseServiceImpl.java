package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.CourseService;
import com.shiyu.ai.education.implementation.domain.model.CourseBO;
import com.shiyu.ai.education.implementation.domain.port.repository.CourseRepository;
import com.shiyu.ai.education.implementation.web.dto.CourseProgressResponse;
import com.shiyu.ai.education.implementation.web.dto.CourseResponse;
import com.shiyu.ai.education.implementation.web.request.CourseRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code CourseServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    /**
     * 课程仓储，表示当前对象中的对应属性。
     */
    private final CourseRepository courseRepository;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public CourseResponse getById(ActorContext actor, Long id) {
        CourseBO bo = courseRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, CourseResponse.class);
    }

    /**
     * {@code listBySubjectCode} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param subjectCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<CourseResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<CourseBO> boList =
                courseRepository.selectBySubjectCode(requireActor(actor).tenantId(), subjectCode);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    /**
     * {@code listByGrade} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param grade 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<CourseResponse> listByGrade(ActorContext actor, Integer grade) {
        List<CourseBO> boList =
                courseRepository.selectByGrade(requireActor(actor).tenantId(), grade);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PageData<CourseResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<CourseBO> boPage =
                courseRepository.selectPage(requireActor(actor).tenantId(), pageNum, pageSize);
        List<CourseResponse> items =
                MapstructUtils.convert(boPage.getItems(), CourseResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    /**
     * {@code getProgress} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param courseId 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public CourseProgressResponse getProgress(ActorContext actor, Long courseId, Long studentId) {
        requireActor(actor);
        return new CourseProgressResponse(courseId, null, 0, 0, 0.0);
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponse create(ActorContext actor, CourseRequest request) {
        actor = requireActor(actor);
        CourseBO bo = new CourseBO();
        bo.setName(request.getName());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setDescription(request.getDescription());
        bo.setCoverUrl(request.getCoverUrl());
        bo.setTextbookId(request.getTextbookId());
        bo.setTeacherId(request.getTeacherId());
        bo.setTotalHours(request.getTotalHours());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        courseRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, CourseResponse.class);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, CourseRequest request) {
        actor = requireActor(actor);
        CourseBO bo = courseRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setDescription(request.getDescription());
            bo.setCoverUrl(request.getCoverUrl());
            bo.setTextbookId(request.getTextbookId());
            bo.setTeacherId(request.getTeacherId());
            bo.setTotalHours(request.getTotalHours());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            courseRepository.update(actor.tenantId(), bo);
        }
    }

    /**
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        courseRepository.deleteById(requireActor(actor).tenantId(), id);
    }

    private static ActorContext requireActor(ActorContext actor) {
        return java.util.Objects.requireNonNull(actor, "actor is required");
    }
}
