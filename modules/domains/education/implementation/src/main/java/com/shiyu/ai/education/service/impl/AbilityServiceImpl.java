package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.port.repository.AbilityRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AbilityServiceImpl implements AbilityService {

    private final AbilityRepository abilityRepository;

    public AbilityServiceImpl(AbilityRepository abilityRepository) {
        this.abilityRepository = abilityRepository;
    }

    @Override
    public AbilityValue get(ActorContext actor, Long studentId, Long knowledgeId) {
        requireActor(actor);
        AbilityBO d = abilityRepository.selectByStudentAndKnowledge(actor.tenantId(), studentId, knowledgeId);
        return d != null ? fromDO(d) : AbilityValue.empty(studentId, knowledgeId);
    }

    @Override
    public void update(ActorContext actor, Long studentId, Long knowledgeId, BloomTaxonomy dimension, double accuracy) {
        requireActor(actor);
        AbilityBO d = abilityRepository.selectByStudentAndKnowledge(actor.tenantId(), studentId, knowledgeId);
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
            d.getRemember() * 0.15 +
            d.getUnderstand() * 0.20 +
            d.getApply() * 0.25 +
            d.getAnalyze() * 0.20 +
            d.getEvaluate() * 0.10 +
            d.getCreateScore() * 0.10
        );
        d.setLastUpdate(LocalDateTime.now());

        if (isNew) {
            abilityRepository.insert(actor.tenantId(), d);
        } else {
            abilityRepository.update(actor.tenantId(), d);
        }

        log.info("能力值更新: student={}, knowledge={}, dimension={}, score={}",
                studentId, knowledgeId, dimension, updated);
    }

    private double getScore(AbilityBO d, BloomTaxonomy dim) {
        return switch (dim) {
            case REMEMBER   -> d.getRemember();
            case UNDERSTAND -> d.getUnderstand();
            case APPLY      -> d.getApply();
            case ANALYZE    -> d.getAnalyze();
            case EVALUATE   -> d.getEvaluate();
            case CREATE     -> d.getCreateScore();
        };
    }

    private void setScore(AbilityBO d, BloomTaxonomy dim, double score) {
        switch (dim) {
            case REMEMBER   -> d.setRemember(score);
            case UNDERSTAND -> d.setUnderstand(score);
            case APPLY      -> d.setApply(score);
            case ANALYZE    -> d.setAnalyze(score);
            case EVALUATE   -> d.setEvaluate(score);
            case CREATE     -> d.setCreateScore(score);
        }
    }

    private AbilityValue fromDO(AbilityBO d) {
        return new AbilityValue(
                d.getStudentId(), d.getKnowledgeId(),
                d.getRemember(), d.getUnderstand(), d.getApply(),
                d.getAnalyze(), d.getEvaluate(), d.getCreateScore(),
                d.getLastUpdate());
    }

    private static void requireActor(ActorContext actor) {
        java.util.Objects.requireNonNull(actor, "actor must not be null");
    }
}
