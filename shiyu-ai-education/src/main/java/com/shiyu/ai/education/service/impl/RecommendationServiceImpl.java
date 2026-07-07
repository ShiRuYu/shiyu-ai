package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.dal.dataobject.education.AbilityDO;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.education.service.QuestionService;
import com.shiyu.ai.education.service.RecommendationService;
import com.shiyu.ai.dal.repository.education.AbilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final AbilityRepository abilityRepository;
    private final QuestionService questionService;

    @Override
    public List<Map<String, Object>> recommendKnowledge(Long studentId, int topK) {
        List<AbilityDO> all = abilityRepository.selectByStudent(studentId);
        return all.stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .sorted(Comparator.comparingDouble(AbilityDO::getOverallMastery))
                .limit(topK)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("knowledgeId", a.getKnowledgeId());
                    m.put("mastery", a.getOverallMastery());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> recommendQuestions(Long studentId, int count) {
        List<AbilityDO> weak = abilityRepository.selectByStudent(studentId).stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .toList();
        if (weak.isEmpty()) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (AbilityDO a : weak) {
            if (result.size() >= count) break;
            int difficulty = a.getOverallMastery() < 40 ? 1 : 2;
            List<QuestionDO> questions = questionService.listByDifficulty(difficulty);
            for (QuestionDO q : questions) {
                if (result.size() >= count) break;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("questionId", q.getId());
                m.put("title", q.getTitle());
                m.put("difficulty", q.getDifficulty());
                result.add(m);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> recommendResources(Long studentId, int topK) {
        return List.of();
    }
}
