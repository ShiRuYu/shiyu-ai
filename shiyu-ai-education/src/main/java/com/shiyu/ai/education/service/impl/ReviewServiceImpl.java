package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.ReviewTaskBO;
import com.shiyu.ai.dal.repository.education.ReviewTaskRepository;
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
    public List<ReviewTaskResponse> listByStudentAndStatus(Long studentId, String status) {
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
        bo.setReviewDate(java.time.LocalDate.now());
        bo.setReviewRound(request.getReviewRound() != null ? request.getReviewRound() : 1);
        bo.setStatus("PENDING");
        reviewTaskRepository.insert(bo);
        return MapstructUtils.convert(bo, ReviewTaskResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ReviewRequest request) {
        ReviewTaskBO bo = reviewTaskRepository.selectById(request.getId());
        if (bo != null) {
            bo.setStatus(request.getStatus());
            reviewTaskRepository.update(bo);
        }
    }
}
