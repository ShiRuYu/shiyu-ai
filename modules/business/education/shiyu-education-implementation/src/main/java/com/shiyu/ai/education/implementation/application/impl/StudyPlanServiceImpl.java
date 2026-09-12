package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
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
 * {@code StudyPlanServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
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
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public StudyPlanResponse getById(ActorContext actor, Long id) {
        StudyPlanBO bo = studyPlanRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, StudyPlanResponse.class);
    }

    /**
     * {@code listByStudentId} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<StudyPlanResponse> listByStudentId(ActorContext actor, Long studentId) {
        List<StudyPlanBO> boList =
                studyPlanRepository.selectByStudentId(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, StudyPlanResponse.class);
    }

    /**
     * {@code listActiveByStudent} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<StudyPlanResponse> listActiveByStudent(ActorContext actor, Long studentId) {
        List<StudyPlanBO> boList =
                studyPlanRepository.selectActiveByStudent(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, StudyPlanResponse.class);
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
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
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
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        studyPlanRepository.deleteById(actor.tenantId(), id);
    }

    /**
     * {@code getTodayTasks} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
