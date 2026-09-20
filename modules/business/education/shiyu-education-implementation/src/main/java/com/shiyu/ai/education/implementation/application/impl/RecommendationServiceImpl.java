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
 * 提供 推荐 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 执行 推荐 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 推荐 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param count 用于完成本次业务处理的 count 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 推荐 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ResourceRecommendResponse> recommendResources(
            ActorContext actor, Long studentId, int topK) {
        return List.of();
    }

    /**
     * 执行 推荐 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param count 用于完成本次业务处理的 count 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 推荐 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @param overallAdvice 用于完成本次业务处理的 overallAdvice 参数。
     * @return 返回 推荐 相关操作生成的结果数据。
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
     * 查询 推荐 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param studentId 用于定位student的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> getWeakKnowledgeIds(ActorContext actor, Long studentId) {
        return abilityRepository.selectByStudent(actor.tenantId(), studentId).stream()
                .filter(a -> a.getOverallMastery() != null && a.getOverallMastery() < 60)
                .map(AbilityBO::getKnowledgeId)
                .collect(Collectors.toList());
    }
}
