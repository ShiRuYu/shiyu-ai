package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.education.domain.model.QuestionBO;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.dto.*;
import com.shiyu.ai.education.service.QuestionService;
import com.shiyu.ai.education.service.RecommendationService;
import com.shiyu.ai.education.port.repository.AbilityRepository;
import com.shiyu.ai.education.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final AbilityRepository abilityRepository;
    private final QuestionService questionService;
    private final ReviewService reviewService;

    @Override
    public List<KnowledgeRecommendResponse> recommendKnowledge(ActorContext actor, Long studentId, int topK) {
        List<AbilityBO> all = abilityRepository.selectByStudent(actor.tenantId(), studentId);
        return all.stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .sorted(Comparator.comparingDouble(AbilityBO::getOverallMastery))
                .limit(topK)
                .map(a -> new KnowledgeRecommendResponse(
                        a.getKnowledgeId(),
                        null,
                        a.getOverallMastery(),
                        "WEAK_POINT",
                        "掌握度偏低，建议加强学习",
                        Math.max(0, 100 - a.getOverallMastery().intValue())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionRecommendResponse> recommendQuestions(ActorContext actor, Long studentId, int count) {
        List<AbilityBO> weak = abilityRepository.selectByStudent(actor.tenantId(), studentId).stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .toList();
        if (weak.isEmpty()) return List.of();

        List<QuestionRecommendResponse> result = new ArrayList<>();
        for (AbilityBO a : weak) {
            if (result.size() >= count) break;
            int difficulty = a.getOverallMastery() < 40 ? 1 : 2;
            List<QuestionResponse> questions = questionService.listByDifficulty(actor, difficulty);
            for (QuestionResponse q : questions) {
                if (result.size() >= count) break;
                result.add(new QuestionRecommendResponse(
                        q.id(),
                        q.title(),
                        q.type(),
                        q.difficulty(),
                        a.getKnowledgeId(),
                        null,
                        "WEAK_POINT_PRACTICE",
                        "基于薄弱知识点推荐",
                        100 - difficulty * 20
                ));
            }
        }
        return result;
    }

    @Override
    public List<ResourceRecommendResponse> recommendResources(ActorContext actor, Long studentId, int topK) {
        return List.of();
    }

    @Override
    public List<QuestionRecommendResponse> recommendReviewTasks(ActorContext actor, Long studentId, int count) {
        List<ReviewTaskResponse> tasks = reviewService.listTodayTasks(actor, studentId);
        return tasks.stream()
                .limit(count)
                .map(t -> new QuestionRecommendResponse(
                        t.knowledgeId(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        "REVIEW",
                        "到期复习任务",
                        80
                ))
                .collect(Collectors.toList());
    }

    @Override
    public HybridRecommendResponse hybridRecommend(ActorContext actor, Long studentId, String overallAdvice) {
        List<KnowledgeRecommendResponse> knowledgeTop = recommendKnowledge(actor, studentId, 5);
        List<QuestionRecommendResponse> questionTop = recommendQuestions(actor, studentId, 10);
        List<ResourceRecommendResponse> resourceTop = recommendResources(actor, studentId, 5);
        List<QuestionRecommendResponse> reviewTop = recommendReviewTasks(actor, studentId, 5);

        return new HybridRecommendResponse(
                studentId,
                knowledgeTop,
                questionTop,
                resourceTop,
                reviewTop,
                overallAdvice,
                System.currentTimeMillis()
        );
    }

    @Override
    public List<Long> getWeakKnowledgeIds(ActorContext actor, Long studentId) {
        return abilityRepository.selectByStudent(actor.tenantId(), studentId).stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .map(AbilityBO::getKnowledgeId)
                .collect(Collectors.toList());
    }
}
