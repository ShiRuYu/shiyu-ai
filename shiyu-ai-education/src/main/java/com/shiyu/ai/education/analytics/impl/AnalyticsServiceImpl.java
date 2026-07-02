package com.shiyu.ai.education.analytics.impl;

import com.shiyu.ai.dal.dataobject.education.AbilityDO;
import com.shiyu.ai.dal.dataobject.education.StudyRecordDO;
import com.shiyu.ai.education.analytics.AnalyticsService;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import com.shiyu.ai.education.dto.AbilityRadarResponse;
import com.shiyu.ai.education.dto.OverviewResponse;
import com.shiyu.ai.education.dto.TrendResponse;
import com.shiyu.ai.education.dto.WeakPointResponse;
import com.shiyu.ai.education.repository.AbilityRepository;
import com.shiyu.ai.education.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final StudyRecordRepository studyRecordRepository;
    private final AbilityRepository abilityRepository;

    @Override
    public List<StudyRecordDO> listRecordsByStudent(Long studentId) {
        return studyRecordRepository.selectByStudentId(studentId);
    }

    @Override
    public List<StudyRecordDO> listRecordsByStudentAndKnowledge(Long studentId, Long knowledgeId) {
        return studyRecordRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyRecordDO createRecord(StudyRecordDO record) {
        studyRecordRepository.insert(record);
        return record;
    }

    @Override
    public AbilityRadarResponse getAbilityRadar(Long studentId, Long knowledgeId) {
        AbilityDO d = abilityRepository.selectByStudentAndKnowledge(studentId, knowledgeId);
        Map<String, Double> abilities = new LinkedHashMap<>();
        abilities.put("remember", d != null ? d.getRemember() : 0.0);
        abilities.put("understand", d != null ? d.getUnderstand() : 0.0);
        abilities.put("apply", d != null ? d.getApply() : 0.0);
        abilities.put("analyze", d != null ? d.getAnalyze() : 0.0);
        abilities.put("evaluate", d != null ? d.getEvaluate() : 0.0);
        abilities.put("create", d != null ? d.getCreateScore() : 0.0);
        double overall = d != null ? (d.getOverallMastery() != null ? d.getOverallMastery() : 0.0) : 0.0;
        return new AbilityRadarResponse(studentId, knowledgeId, abilities, overall);
    }

    @Override
    public OverviewResponse getOverview(Long studentId) {
        List<StudyRecordDO> records = studyRecordRepository.selectByStudentId(studentId);
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
        for (StudyRecordDO r : records) {
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
        List<AbilityDO> abilities = abilityRepository.selectByStudent(studentId);
        return abilities.stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .map(a -> new WeakPointResponse(a.getKnowledgeId(), null, a.getOverallMastery()))
                .sorted(Comparator.comparingDouble(WeakPointResponse::mastery))
                .collect(Collectors.toList());
    }

    @Override
    public TrendResponse getTrend(Long studentId) {
        List<StudyRecordDO> records = studyRecordRepository.selectByStudentId(studentId);
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
}
