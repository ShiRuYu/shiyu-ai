package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
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
 * 提供 课程 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 课程 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 课程 相关操作生成的结果数据。
     */
    @Override
    public CourseResponse getById(ActorContext actor, Long id) {
        CourseBO bo = courseRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, CourseResponse.class);
    }

    /**
     * 查询 课程 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<CourseResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<CourseBO> boList =
                courseRepository.selectBySubjectCode(requireActor(actor).tenantId(), subjectCode);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    /**
     * 查询 课程 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param grade 用于完成本次业务处理的 grade 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<CourseResponse> listByGrade(ActorContext actor, Integer grade) {
        List<CourseBO> boList =
                courseRepository.selectByGrade(requireActor(actor).tenantId(), grade);
        return MapstructUtils.convert(boList, CourseResponse.class);
    }

    /**
     * 查询 课程 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 课程 相关操作生成的结果数据。
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
     * 查询 课程 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param courseId 用于定位course的标识。
     * @param studentId 用于定位student的标识。
     * @return 返回 课程 相关操作生成的结果数据。
     */
    @Override
    public CourseProgressResponse getProgress(ActorContext actor, Long courseId, Long studentId) {
        requireActor(actor);
        return new CourseProgressResponse(courseId, null, 0, 0, 0.0);
    }

    /**
     * 执行 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 课程 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
