package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
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
 * 提供 复习 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 复习 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 复习 相关操作生成的结果数据。
     */
    @Override
    public ReviewTaskResponse getById(ActorContext actor, Long id) {
        ReviewTaskBO bo = reviewTaskRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

    /**
     * 查询 复习 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ReviewTaskResponse> listTodayTasks(ActorContext actor, Long studentId) {
        List<ReviewTaskBO> boList =
                reviewTaskRepository.selectTodayTasks(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    /**
     * 查询 复习 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ReviewTaskResponse> listByStudentAndStatus(
            ActorContext actor, Long studentId, Integer status) {
        List<ReviewTaskBO> boList =
                reviewTaskRepository.selectByStudentAndStatus(actor.tenantId(), studentId, status);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    /**
     * 查询 复习 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 复习 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActorContext actor, Long id) {
        reviewTaskRepository.deleteById(actor.tenantId(), id);
    }
}
