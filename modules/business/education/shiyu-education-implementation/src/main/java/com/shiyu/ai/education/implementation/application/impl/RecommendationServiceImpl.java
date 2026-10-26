package com.shiyu.ai.education.implementation.application.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.education.implementation.application.QuestionService;
import com.shiyu.ai.education.implementation.application.RecommendationService;
import com.shiyu.ai.education.implementation.application.ResourceService;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.domain.model.AbilityBO;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.port.repository.AbilityRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.persistence.dataobject.ResourceKnowledgeDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ResourceKnowledgeMapper;
import com.shiyu.ai.education.implementation.web.dto.*;
import com.shiyu.ai.education.implementation.web.dto.QuestionResponse;
import com.shiyu.ai.education.implementation.web.dto.ResourceResponse;
import com.shiyu.ai.education.implementation.web.dto.ReviewTaskResponse;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 提供 推荐 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
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
    private final ResourceService resourceService;
    private final StudentRepository studentRepository;
    private final ResourceKnowledgeMapper resourceKnowledgeMapper;
    private final KnowledgePointPort knowledgePointPort;

    @Autowired
    public RecommendationServiceImpl(
            AbilityRepository abilityRepository,
            QuestionService questionService,
            ReviewService reviewService,
            ResourceService resourceService,
            StudentRepository studentRepository,
            ResourceKnowledgeMapper resourceKnowledgeMapper,
            KnowledgePointPort knowledgePointPort) {
        this.abilityRepository = abilityRepository;
        this.questionService = questionService;
        this.reviewService = reviewService;
        this.resourceService = resourceService;
        this.studentRepository = studentRepository;
        this.resourceKnowledgeMapper = resourceKnowledgeMapper;
        this.knowledgePointPort = knowledgePointPort;
    }

    /** Keeps the existing lightweight constructor usable by current unit callers. */
    RecommendationServiceImpl(
            AbilityRepository abilityRepository,
            QuestionService questionService,
            ReviewService reviewService) {
        this(abilityRepository, questionService, reviewService, null, null, null, null);
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
        if (topK <= 0
                || studentId == null
                || actor == null
                || actor.tenantId() == null
                || resourceService == null
                || studentRepository == null
                || resourceKnowledgeMapper == null
                || knowledgePointPort == null) {
            return List.of();
        }

        StudentBO student = studentRepository.selectById(actor.tenantId(), studentId);
        if (student == null
                || !Integer.valueOf(1).equals(student.getStatus())
                || student.getGrade() == null) {
            return List.of();
        }

        List<AbilityBO> abilities = abilityRepository.selectByStudent(actor.tenantId(), studentId);
        if (abilities == null || abilities.isEmpty()) return List.of();

        Map<Long, Double> weakestMasteryByKnowledge = new HashMap<>();
        for (AbilityBO ability : abilities) {
            if (ability == null
                    || ability.getKnowledgeId() == null
                    || ability.getOverallMastery() == null
                    || ability.getOverallMastery() >= 60) {
                continue;
            }
            weakestMasteryByKnowledge.merge(
                    ability.getKnowledgeId(), ability.getOverallMastery(), Math::min);
        }
        if (weakestMasteryByKnowledge.isEmpty()) return List.of();

        Map<Long, KnowledgeResponse> knowledgeById = new HashMap<>();
        List<KnowledgeResponse> knowledgePoints = knowledgePointPort.listStudentEducationPoints(actor);
        if (knowledgePoints == null || knowledgePoints.isEmpty()) return List.of();
        for (KnowledgeResponse knowledge : knowledgePoints) {
            if (knowledge != null
                    && knowledge.id() != null
                    && weakestMasteryByKnowledge.containsKey(knowledge.id())
                    && normalizeSubject(knowledge.category()) != null) {
                knowledgeById.putIfAbsent(knowledge.id(), knowledge);
            }
        }
        if (knowledgeById.isEmpty()) return List.of();

        List<ResourceResponse> resources = resourceService.listAll(actor);
        if (resources == null || resources.isEmpty()) return List.of();
        Map<Long, ResourceResponse> resourceById = new HashMap<>();
        for (ResourceResponse resource : resources) {
            if (resource != null && resource.id() != null) {
                resourceById.putIfAbsent(resource.id(), resource);
            }
        }
        if (resourceById.isEmpty()) return List.of();

        List<ResourceKnowledgeDO> associations =
                resourceKnowledgeMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", actor.tenantId().value())
                                .eq("status", 1)
                                .eq("del_flag", 0)
                                .in("knowledge_id", knowledgeById.keySet()));
        if (associations == null || associations.isEmpty()) return List.of();

        List<ResourceCandidate> candidates = new ArrayList<>();
        for (ResourceKnowledgeDO association : associations) {
            if (association == null
                    || association.getResourceId() == null
                    || association.getKnowledgeId() == null) {
                continue;
            }
            ResourceResponse resource = resourceById.get(association.getResourceId());
            KnowledgeResponse knowledge = knowledgeById.get(association.getKnowledgeId());
            if (resource == null
                    || knowledge == null
                    || !Objects.equals(student.getGrade(), resource.grade())
                    || !Objects.equals(
                            normalizeSubject(knowledge.category()),
                            normalizeSubject(resource.subjectCode()))) {
                continue;
            }
            candidates.add(
                    new ResourceCandidate(
                            resource,
                            knowledge,
                            weakestMasteryByKnowledge.get(association.getKnowledgeId()),
                            association.getSortOrder()));
        }

        candidates.sort(
                Comparator.comparingDouble(ResourceCandidate::mastery)
                        .thenComparing(
                                candidate ->
                                        candidate.sortOrder() == null
                                                ? Integer.MAX_VALUE
                                                : candidate.sortOrder())
                        .thenComparing(candidate -> candidate.resource().id())
                        .thenComparing(candidate -> candidate.knowledge().id()));

        List<ResourceRecommendResponse> recommendations = new ArrayList<>();
        Set<Long> recommendedResourceIds = new HashSet<>();
        for (ResourceCandidate candidate : candidates) {
            ResourceResponse resource = candidate.resource();
            if (!recommendedResourceIds.add(resource.id())) continue;
            int score = (int) Math.round(100 - candidate.mastery());
            recommendations.add(
                    new ResourceRecommendResponse(
                            resource.id(),
                            resource.name(),
                            resource.type(),
                            candidate.knowledge().id(),
                            candidate.knowledge().name(),
                            "WEAK_POINT_RESOURCE",
                            "基于薄弱知识点推荐学习资源",
                            Math.max(0, Math.min(100, score))));
            if (recommendations.size() >= topK) break;
        }
        return List.copyOf(recommendations);
    }

    private static String normalizeSubject(String subject) {
        return subject == null || subject.isBlank()
                ? null
                : subject.strip().toUpperCase(Locale.ROOT);
    }

    private record ResourceCandidate(
            ResourceResponse resource,
            KnowledgeResponse knowledge,
            double mastery,
            Integer sortOrder) {}

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
