package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.enums.StudyPlanStatus;
import com.shiyu.ai.education.domain.enums.StudyPlanItemStatus;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.StudyPlanBO;
import com.shiyu.ai.education.domain.model.StudyPlanItemBO;
import com.shiyu.ai.education.port.repository.StudyPlanItemRepository;
import com.shiyu.ai.education.port.repository.StudyPlanRepository;
import com.shiyu.ai.education.dto.DailyTaskResponse;
import com.shiyu.ai.education.dto.StudyPlanResponse;
import com.shiyu.ai.education.request.StudyPlanRequest;
import com.shiyu.ai.education.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudyPlanItemRepository studyPlanItemRepository;

    @Override
    public StudyPlanResponse getById(Long id) {
        StudyPlanBO bo = studyPlanRepository.selectById(id);
        return MapstructUtils.convert(bo, StudyPlanResponse.class);
    }

    @Override
    public List<StudyPlanResponse> listByStudentId(Long studentId) {
        List<StudyPlanBO> boList = studyPlanRepository.selectByStudentId(studentId);
        return MapstructUtils.convert(boList, StudyPlanResponse.class);
    }

    @Override
    public List<StudyPlanResponse> listActiveByStudent(Long studentId) {
        List<StudyPlanBO> boList = studyPlanRepository.selectActiveByStudent(studentId);
        return MapstructUtils.convert(boList, StudyPlanResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyPlanResponse create(StudyPlanRequest request) {
        StudyPlanBO bo = new StudyPlanBO();
        bo.setStudentId(request.getStudentId());
        bo.setName(request.getName());
        if (request.getStartDate() != null) bo.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) bo.setEndDate(request.getEndDate());
        bo.setStatus(StudyPlanStatus.ACTIVE.getCode());
        studyPlanRepository.insert(bo);
        return MapstructUtils.convert(bo, StudyPlanResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StudyPlanRequest request) {
        StudyPlanBO bo = studyPlanRepository.selectById(request.getId());
        if (bo != null) {
            if (request.getName() != null) bo.setName(request.getName());
            if (request.getStartDate() != null) bo.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) bo.setEndDate(request.getEndDate());
            if (request.getStatus() != null) bo.setStatus(Integer.valueOf(request.getStatus()));
            studyPlanRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        studyPlanRepository.deleteById(id);
    }

    @Override
    public List<DailyTaskResponse> getTodayTasks(Long studentId) {
        List<StudyPlanBO> plans = studyPlanRepository.selectByStudentId(studentId);
        List<Long> planIds = plans.stream().map(StudyPlanBO::getId).collect(Collectors.toList());
        if (planIds.isEmpty()) return List.of();
        List<StudyPlanItemBO> items = studyPlanItemRepository.selectTodayItems(planIds);
        return items.stream().map(item -> new DailyTaskResponse(
                item.getId(),
                item.getKnowledgeId(),
                null,
                item.getPlanDate().toString(),
                item.getStatus(),
                item.getStatusDesc(),
                item.getOrderNo()
        )).collect(Collectors.toList());
    }
}
