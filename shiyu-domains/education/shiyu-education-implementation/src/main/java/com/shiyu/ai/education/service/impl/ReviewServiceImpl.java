package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.enums.ReviewTaskStatus;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.port.repository.ReviewTaskRepository;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.request.ReviewRequest;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewTaskRepository reviewTaskRepository;

    @Override
    public ReviewTaskResponse getById(ActorContext actor, Long id) {
        ReviewTaskBO bo = reviewTaskRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

    @Override
    public List<ReviewTaskResponse> listTodayTasks(ActorContext actor, Long studentId) {
        List<ReviewTaskBO> boList = reviewTaskRepository.selectTodayTasks(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    @Override
    public List<ReviewTaskResponse> listByStudentAndStatus(ActorContext actor, Long studentId, Integer status) {
        List<ReviewTaskBO> boList = reviewTaskRepository.selectByStudentAndStatus(actor.tenantId(), studentId, status);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    @Override
    public List<ReviewTaskResponse> listByStudentAndKnowledge(ActorContext actor, Long studentId, Long knowledgeId) {
        List<ReviewTaskBO> boList = reviewTaskRepository.selectByStudentAndKnowledge(actor.tenantId(), studentId, knowledgeId);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewTaskResponse create(ActorContext actor, ReviewRequest request) {
        ReviewTaskBO bo = new ReviewTaskBO();
        bo.setStudentId(request.getStudentId());
        bo.setKnowledgeId(request.getKnowledgeId());
        bo.setReviewDate(request.getReviewDate() != null
                ? request.getReviewDate() : java.time.LocalDate.now());
        bo.setReviewRound(request.getReviewRound() != null ? request.getReviewRound() : 1);
        bo.setStatus(request.getStatus() != null
                ? request.getStatus() : ReviewTaskStatus.PENDING.getCode());
        bo.setResultScore(request.getResultScore());
        reviewTaskRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActorContext actor, Long id) {
        reviewTaskRepository.deleteById(actor.tenantId(), id);
    }
}
