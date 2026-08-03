package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import com.shiyu.ai.education.port.repository.AbilityRepository;
import com.shiyu.ai.education.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.dto.*;
import com.shiyu.ai.education.request.StudyRecordRequest;
import com.shiyu.ai.education.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final StudyRecordRepository studyRecordRepository;
    private final AbilityRepository abilityRepository;

    @Override
    public List<StudyRecordResponse> listRecordsByStudent(Long studentId) {
        List<StudyRecordBO> boList = studyRecordRepository.selectByStudent(studentId);
        return MapstructUtils.convert(boList, StudyRecordResponse.class);
    }

    @Override
    public List<StudyRecordResponse> listRecordsByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        List<StudyRecordBO> boList = studyRecordRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
        return MapstructUtils.convert(boList, StudyRecordResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyRecordResponse createRecord(StudyRecordRequest request) {
        StudyRecordBO bo = new StudyRecordBO();
        bo.setStudentId(request.getStudentId());
        bo.setKnowledgeId(request.getKnowledgeId());
        bo.setRecordType(request.getRecordType());
        bo.setQuestionId(request.getQuestionId());
        bo.setScore(request.getScore());
        bo.setAccuracy(request.getAccuracy());
        bo.setDurationSec(request.getDurationSec());
        studyRecordRepository.insert(bo);
        return MapstructUtils.convert(bo, StudyRecordResponse.class);
    }

    @Override
    public AbilityRadarResponse getAbilityRadar(Long studentId, Long knowledgeId) {
        AbilityBO ability = abilityRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
        Map<String, Double> dimensions = new LinkedHashMap<>();
        dimensions.put("remember", valueOf(ability == null ? null : ability.getRemember()));
        dimensions.put("understand", valueOf(ability == null ? null : ability.getUnderstand()));
        dimensions.put("apply", valueOf(ability == null ? null : ability.getApply()));
        dimensions.put("analyze", valueOf(ability == null ? null : ability.getAnalyze()));
        dimensions.put("evaluate", valueOf(ability == null ? null : ability.getEvaluate()));
        dimensions.put("create", valueOf(ability == null ? null : ability.getCreateScore()));
        return new AbilityRadarResponse(
                studentId,
                knowledgeId,
                dimensions,
                valueOf(ability == null ? null : ability.getOverallMastery()));
    }

    @Override
    public OverviewResponse getOverview(Long studentId) {
        List<StudyRecordBO> records = studyRecordRepository.selectByStudent(studentId);
        if (records.isEmpty()) {
            return new OverviewResponse(0, 0, 0, 0, 0.0, 0.0, 0);
        }
        Set<Long> knowledgeLearned = new HashSet<>();
        Set<Long> knowledgePracticed = new HashSet<>();
        int totalQuestions = 0;
        double totalAccuracy = 0;
        int accuracyCount = 0;
        long totalDurationSec = 0;
        Set<String> studyDays = new HashSet<>();
        for (StudyRecordBO r : records) {
            if (r.getKnowledgeId() != null) knowledgeLearned.add(r.getKnowledgeId());
            if (r.getRecordType() != null && "PRACTICE".equals(r.getRecordType())) {
                if (r.getKnowledgeId() != null) knowledgePracticed.add(r.getKnowledgeId());
                totalQuestions++;
            }
            if (r.getAccuracy() != null) { totalAccuracy += r.getAccuracy(); accuracyCount++; }
            if (r.getDurationSec() != null) totalDurationSec += r.getDurationSec();
            if (r.getCreateTime() != null) studyDays.add(r.getCreateTime().toLocalDate().toString());
        }
        double avgAccuracy = accuracyCount > 0 ? totalAccuracy / accuracyCount * 100 : 0.0;
        double weeklyHours = totalDurationSec / 3600.0;
        return new OverviewResponse(
                studyDays.size(), knowledgeLearned.size(), knowledgePracticed.size(),
                totalQuestions, Math.round(avgAccuracy * 10.0) / 10.0,
                Math.round(weeklyHours * 10.0) / 10.0, studyDays.size());
    }

    @Override
    public List<WeakPointResponse> getWeakPoints(Long studentId) {
        List<AbilityBO> abilities = abilityRepository.selectByStudent(studentId);
        return abilities.stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .map(a -> new WeakPointResponse(a.getKnowledgeId(), null, a.getOverallMastery()))
                .sorted(Comparator.comparingDouble(WeakPointResponse::mastery))
                .collect(Collectors.toList());
    }

    @Override
    public TrendResponse getTrend(Long studentId) {
        List<StudyRecordBO> records = studyRecordRepository.selectByStudent(studentId);
        List<String> dates = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.toString());
            long count = records.stream()
                    .filter(r -> r.getCreateTime() != null
                            && r.getCreateTime().toLocalDate().equals(day))
                    .count();
            values.add((double) count);
        }
        return new TrendResponse(dates, values);
    }

    private static double valueOf(Double value) {
        return value == null ? 0.0 : value;
    }
}
