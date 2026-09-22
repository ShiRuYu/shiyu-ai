package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.StudyPlanService;
import com.shiyu.ai.education.implementation.domain.enums.StudyPlanStatus;
import com.shiyu.ai.education.implementation.domain.model.StudyPlanBO;
import com.shiyu.ai.education.implementation.domain.model.StudyPlanItemBO;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyPlanItemRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyPlanRepository;
import com.shiyu.ai.education.implementation.web.dto.DailyTaskResponse;
import com.shiyu.ai.education.implementation.web.dto.StudyPlanResponse;
import com.shiyu.ai.education.implementation.web.request.StudyPlanRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 提供 Study Plan 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    /**
     * studyPlanRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final StudyPlanRepository studyPlanRepository;
    /**
     * studyPlanItemRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final StudyPlanItemRepository studyPlanItemRepository;

    /**
     * 查询 Study Plan 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Study Plan 相关操作生成的结果数据。
     */
    @Override
    public StudyPlanResponse getById(ActorContext actor, Long id) {
        StudyPlanBO bo = studyPlanRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, StudyPlanResponse.class);
    }

    /**
     * 查询 Study Plan 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<StudyPlanResponse> listByStudentId(ActorContext actor, Long studentId) {
        List<StudyPlanBO> boList =
                studyPlanRepository.selectByStudentId(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, StudyPlanResponse.class);
    }

    /**
     * 查询 Study Plan 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<StudyPlanResponse> listActiveByStudent(ActorContext actor, Long studentId) {
        List<StudyPlanBO> boList =
                studyPlanRepository.selectActiveByStudent(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, StudyPlanResponse.class);
    }

    /**
     * 执行 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanResponse create(ActorContext actor, StudyPlanRequest request) {
        StudyPlanBO bo = new StudyPlanBO();
        bo.setStudentId(request.getStudentId());
        bo.setName(request.getName());
        if (request.getStartDate() != null) bo.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) bo.setEndDate(request.getEndDate());
        bo.setStatus(StudyPlanStatus.ACTIVE.getCode());
        studyPlanRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, StudyPlanResponse.class);
    }

    /**
     * 执行 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, StudyPlanRequest request) {
        StudyPlanBO bo = studyPlanRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            if (request.getName() != null) bo.setName(request.getName());
            if (request.getStartDate() != null) bo.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) bo.setEndDate(request.getEndDate());
            if (request.getStatus() != null) bo.setStatus(Integer.valueOf(request.getStatus()));
            studyPlanRepository.update(actor.tenantId(), bo);
        }
    }

    /**
     * 执行 Study Plan 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        studyPlanRepository.deleteById(actor.tenantId(), id);
    }

    /**
     * 查询 Study Plan 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<DailyTaskResponse> getTodayTasks(ActorContext actor, Long studentId) {
        List<StudyPlanBO> plans =
                studyPlanRepository.selectByStudentId(actor.tenantId(), studentId);
        List<Long> planIds = plans.stream().map(StudyPlanBO::getId).collect(Collectors.toList());
        if (planIds.isEmpty()) return List.of();
        List<StudyPlanItemBO> items =
                studyPlanItemRepository.selectTodayItems(actor.tenantId(), planIds);
        return items.stream()
                .map(
                        item ->
                                new DailyTaskResponse(
                                        item.getId(),
                                        item.getKnowledgeId(),
                                        null,
                                        item.getPlanDate().toString(),
                                        item.getStatus(),
                                        item.getStatusDesc(),
                                        item.getOrderNo()))
                .collect(Collectors.toList());
    }
}
