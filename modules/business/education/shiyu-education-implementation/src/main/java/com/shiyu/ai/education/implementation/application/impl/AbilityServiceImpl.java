package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.education.implementation.application.AbilityService;
import com.shiyu.ai.education.implementation.domain.AbilityValue;
import com.shiyu.ai.education.implementation.domain.BloomTaxonomy;
import com.shiyu.ai.education.implementation.domain.model.AbilityBO;
import com.shiyu.ai.education.implementation.domain.port.repository.AbilityRepository;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * {@code AbilityServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
public class AbilityServiceImpl implements AbilityService {

    /**
     * abilityRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AbilityRepository abilityRepository;

    /**
     * {@code AbilityServiceImpl} 创建并初始化当前类型实例。
     *
     * @param abilityRepository 参数值，用于执行当前操作。
     */
    public AbilityServiceImpl(AbilityRepository abilityRepository) {
        this.abilityRepository = abilityRepository;
    }

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public AbilityValue get(ActorContext actor, Long studentId, Long knowledgeId) {
        requireActor(actor);
        AbilityBO d =
                abilityRepository.selectByStudentAndKnowledge(
                        actor.tenantId(), studentId, knowledgeId);
        return d != null ? fromDO(d) : AbilityValue.empty(studentId, knowledgeId);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param studentId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     * @param dimension 参数值，用于执行当前操作。
     * @param accuracy 参数值，用于执行当前操作。
     */
    @Override
    public void update(
            ActorContext actor,
            Long studentId,
            Long knowledgeId,
            BloomTaxonomy dimension,
            double accuracy) {
        requireActor(actor);
        AbilityBO d =
                abilityRepository.selectByStudentAndKnowledge(
                        actor.tenantId(), studentId, knowledgeId);
        boolean isNew = false;
        if (d == null) {
            d = new AbilityBO();
            d.setStudentId(studentId);
            d.setKnowledgeId(knowledgeId);
            d.setRemember(0.0);
            d.setUnderstand(0.0);
            d.setApply(0.0);
            d.setAnalyze(0.0);
            d.setEvaluate(0.0);
            d.setCreateScore(0.0);
            isNew = true;
        }

        double current = getScore(d, dimension);
        double updated = current + (100 - current) * accuracy * 0.1;
        updated = Math.min(100, updated);

        setScore(d, dimension, updated);
        d.setOverallMastery(
                d.getRemember() * 0.15
                        + d.getUnderstand() * 0.20
                        + d.getApply() * 0.25
                        + d.getAnalyze() * 0.20
                        + d.getEvaluate() * 0.10
                        + d.getCreateScore() * 0.10);
        d.setLastUpdate(LocalDateTime.now());

        if (isNew) {
            abilityRepository.insert(actor.tenantId(), d);
        } else {
            abilityRepository.update(actor.tenantId(), d);
        }

        log.info(
                "能力值更新: studentIdPresent={}, knowledgeIdPresent={}, dimension={}, score={}",
                studentId != null,
                knowledgeId != null,
                dimension,
                updated);
    }

    private double getScore(AbilityBO d, BloomTaxonomy dim) {
        return switch (dim) {
            case REMEMBER -> d.getRemember();
            case UNDERSTAND -> d.getUnderstand();
            case APPLY -> d.getApply();
            case ANALYZE -> d.getAnalyze();
            case EVALUATE -> d.getEvaluate();
            case CREATE -> d.getCreateScore();
        };
    }

    private void setScore(AbilityBO d, BloomTaxonomy dim, double score) {
        switch (dim) {
            case REMEMBER -> d.setRemember(score);
            case UNDERSTAND -> d.setUnderstand(score);
            case APPLY -> d.setApply(score);
            case ANALYZE -> d.setAnalyze(score);
            case EVALUATE -> d.setEvaluate(score);
            case CREATE -> d.setCreateScore(score);
        }
    }

    private AbilityValue fromDO(AbilityBO d) {
        return new AbilityValue(
                d.getStudentId(),
                d.getKnowledgeId(),
                d.getRemember(),
                d.getUnderstand(),
                d.getApply(),
                d.getAnalyze(),
                d.getEvaluate(),
                d.getCreateScore(),
                d.getLastUpdate());
    }

    private static void requireActor(ActorContext actor) {
        java.util.Objects.requireNonNull(actor, "actor must not be null");
    }
}
