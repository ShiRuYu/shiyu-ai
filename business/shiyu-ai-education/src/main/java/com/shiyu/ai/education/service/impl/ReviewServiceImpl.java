package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.enums.ReviewTaskStatus;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.port.repository.ReviewTaskRepository;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.request.ReviewRequest;
import com.shiyu.ai.education.service.ReviewService;
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
    public ReviewTaskResponse getById(Long id) {
        ReviewTaskBO bo = reviewTaskRepository.selectById(id);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

    @Override
    public List<ReviewTaskResponse> listTodayTasks(Long studentId) {
        List<ReviewTaskBO> boList = reviewTaskRepository.selectTodayTasks(studentId);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    @Override
    public List<ReviewTaskResponse> listByStudentAndStatus(Long studentId, Integer status) {
        List<ReviewTaskBO> boList = reviewTaskRepository.selectByStudentAndStatus(studentId, status);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    @Override
    public List<ReviewTaskResponse> listByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        List<ReviewTaskBO> boList = reviewTaskRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
        return MapstructUtils.convert(boList, ReviewTaskResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewTaskResponse create(ReviewRequest request) {
        ReviewTaskBO bo = new ReviewTaskBO();
        bo.setStudentId(request.getStudentId());
        bo.setKnowledgeId(request.getKnowledgeId());
        bo.setReviewDate(request.getReviewDate() != null
                ? request.getReviewDate() : java.time.LocalDate.now());
        bo.setReviewRound(request.getReviewRound() != null ? request.getReviewRound() : 1);
        bo.setStatus(request.getStatus() != null
                ? request.getStatus() : ReviewTaskStatus.PENDING.getCode());
        bo.setResultScore(request.getResultScore());
        reviewTaskRepository.insert(bo);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ReviewRequest request) {
        ReviewTaskBO bo = reviewTaskRepository.selectById(request.getId());
        if (bo != null) {
            if (request.getStudentId() != null) bo.setStudentId(request.getStudentId());
            if (request.getKnowledgeId() != null) bo.setKnowledgeId(request.getKnowledgeId());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            if (request.getReviewRound() != null) bo.setReviewRound(request.getReviewRound());
            if (request.getReviewDate() != null) bo.setReviewDate(request.getReviewDate());
            if (request.getResultScore() != null) bo.setResultScore(request.getResultScore());
            reviewTaskRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, Double resultScore) {
        ReviewTaskBO task = reviewTaskRepository.selectById(id);
        if (task == null) {
            return;
        }
        task.setStatus(ReviewTaskStatus.COMPLETED.getCode());
        task.setResultScore(resultScore);
        task.setCompletedAt(java.time.LocalDateTime.now());
        reviewTaskRepository.update(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        reviewTaskRepository.deleteById(id);
    }
}
