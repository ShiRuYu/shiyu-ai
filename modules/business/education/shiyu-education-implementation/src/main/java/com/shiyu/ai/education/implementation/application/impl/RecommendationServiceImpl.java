package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.education.implementation.application.QuestionService;
import com.shiyu.ai.education.implementation.application.RecommendationService;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.domain.model.AbilityBO;
import com.shiyu.ai.education.implementation.domain.port.repository.AbilityRepository;
import com.shiyu.ai.education.implementation.web.dto.*;
import com.shiyu.ai.education.implementation.web.dto.QuestionResponse;
import com.shiyu.ai.education.implementation.web.dto.ReviewTaskResponse;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code RecommendationServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    /**
     * abilityRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AbilityRepository abilityRepository;
    /**
     * 题目服务，表示当前对象中的对应属性。
     */
    private final QuestionService questionService;
    /**
     * reviewService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewService reviewService;

    /**
     * {@code recommendKnowledge} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param topK 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeRecommendResponse> recommendKnowledge(
            ActorContext actor, Long studentId, int topK) {
        List<AbilityBO> all = abilityRepository.selectByStudent(actor.tenantId(), studentId);
        return all.stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .sorted(Comparator.comparingDouble(AbilityBO::getOverallMastery))
                .limit(topK)
                .map(
                        a ->
                                new KnowledgeRecommendResponse(
                                        a.getKnowledgeId(),
                                        null,
                                        a.getOverallMastery(),
                                        "WEAK_POINT",
                                        "掌握度偏低，建议加强学习",
                                        Math.max(0, 100 - a.getOverallMastery().intValue())))
                .collect(Collectors.toList());
    }

    /**
     * {@code recommendQuestions} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param count 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<QuestionRecommendResponse> recommendQuestions(
            ActorContext actor, Long studentId, int count) {
        List<AbilityBO> weak =
                abilityRepository.selectByStudent(actor.tenantId(), studentId).stream()
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
                result.add(
                        new QuestionRecommendResponse(
                                q.id(),
                                q.title(),
                                q.type(),
                                q.difficulty(),
                                a.getKnowledgeId(),
                                null,
                                "WEAK_POINT_PRACTICE",
                                "基于薄弱知识点推荐",
                                100 - difficulty * 20));
            }
        }
        return result;
    }

    /**
     * {@code recommendResources} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param topK 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ResourceRecommendResponse> recommendResources(
            ActorContext actor, Long studentId, int topK) {
        return List.of();
    }

    /**
     * {@code recommendReviewTasks} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param count 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<QuestionRecommendResponse> recommendReviewTasks(
            ActorContext actor, Long studentId, int count) {
        List<ReviewTaskResponse> tasks = reviewService.listTodayTasks(actor, studentId);
        return tasks.stream()
                .limit(count)
                .map(
                        t ->
                                new QuestionRecommendResponse(
                                        t.knowledgeId(),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        "REVIEW",
                                        "到期复习任务",
                                        80))
                .collect(Collectors.toList());
    }

    /**
     * {@code hybridRecommend} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param overallAdvice 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public HybridRecommendResponse hybridRecommend(
            ActorContext actor, Long studentId, String overallAdvice) {
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
                System.currentTimeMillis());
    }

    /**
     * {@code getWeakKnowledgeIds} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> getWeakKnowledgeIds(ActorContext actor, Long studentId) {
        return abilityRepository.selectByStudent(actor.tenantId(), studentId).stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .map(AbilityBO::getKnowledgeId)
                .collect(Collectors.toList());
    }
}
