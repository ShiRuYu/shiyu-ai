package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.domain.enums.ReviewTaskStatus;
import com.shiyu.ai.education.implementation.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ReviewTaskRepository;
import com.shiyu.ai.education.implementation.web.dto.ReviewTaskResponse;
import com.shiyu.ai.education.implementation.web.request.ReviewRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code ReviewServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    /**
     * reviewTaskRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewTaskRepository reviewTaskRepository;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ReviewTaskResponse getById(ActorContext actor, Long id) {
        ReviewTaskBO bo = reviewTaskRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

    /**
     * {@code listTodayTasks} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ReviewTaskResponse> listTodayTasks(ActorContext actor, Long studentId) {
        List<ReviewTaskBO> boList =
                reviewTaskRepository.selectTodayTasks(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    /**
     * {@code listByStudentAndStatus} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ReviewTaskResponse> listByStudentAndStatus(
            ActorContext actor, Long studentId, Integer status) {
        List<ReviewTaskBO> boList =
                reviewTaskRepository.selectByStudentAndStatus(actor.tenantId(), studentId, status);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    /**
     * {@code listByStudentAndKnowledge} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ReviewTaskResponse> listByStudentAndKnowledge(
            ActorContext actor, Long studentId, Long knowledgeId) {
        List<ReviewTaskBO> boList =
                reviewTaskRepository.selectByStudentAndKnowledge(
                        actor.tenantId(), studentId, knowledgeId);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
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
    public ReviewTaskResponse create(ActorContext actor, ReviewRequest request) {
        ReviewTaskBO bo = new ReviewTaskBO();
        bo.setStudentId(request.getStudentId());
        bo.setKnowledgeId(request.getKnowledgeId());
        bo.setReviewDate(
                request.getReviewDate() != null
                        ? request.getReviewDate()
                        : java.time.LocalDate.now());
        bo.setReviewRound(request.getReviewRound() != null ? request.getReviewRound() : 1);
        bo.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : ReviewTaskStatus.PENDING.getCode());
        bo.setResultScore(request.getResultScore());
        reviewTaskRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, ReviewRequest request) {
        ReviewTaskBO bo = reviewTaskRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            if (request.getStudentId() != null) bo.setStudentId(request.getStudentId());
            if (request.getKnowledgeId() != null) bo.setKnowledgeId(request.getKnowledgeId());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            if (request.getReviewRound() != null) bo.setReviewRound(request.getReviewRound());
            if (request.getReviewDate() != null) bo.setReviewDate(request.getReviewDate());
            if (request.getResultScore() != null) bo.setResultScore(request.getResultScore());
            reviewTaskRepository.update(actor.tenantId(), bo);
        }
    }

    /**
     * {@code complete} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param resultScore 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(ActorContext actor, Long id, Double resultScore) {
        ReviewTaskBO task = reviewTaskRepository.selectById(actor.tenantId(), id);
        if (task == null) {
            return;
        }
        task.setStatus(ReviewTaskStatus.COMPLETED.getCode());
        task.setResultScore(resultScore);
        task.setCompletedAt(java.time.LocalDateTime.now());
        reviewTaskRepository.update(actor.tenantId(), task);
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActorContext actor, Long id) {
        reviewTaskRepository.deleteById(actor.tenantId(), id);
    }
}
