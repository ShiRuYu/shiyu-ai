package com.shiyu.ai.knowledge.ability.impl;

import com.shiyu.ai.knowledge.domain.AbilityValue;
import com.shiyu.ai.knowledge.domain.BloomTaxonomy;
import com.shiyu.ai.knowledge.ability.AbilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AbilityServiceImpl implements AbilityService {

    private final Map<String, AbilityValue> store = new ConcurrentHashMap<>();

    @Override
    public AbilityValue get(Long studentId, Long knowledgeId) {
        String key = buildKey(studentId, knowledgeId);
        return store.getOrDefault(key, AbilityValue.empty(studentId, knowledgeId));
    }

    @Override
    public void update(Long studentId, Long knowledgeId, BloomTaxonomy dimension, double accuracy) {
        String key = buildKey(studentId, knowledgeId);
        AbilityValue current = store.getOrDefault(key, AbilityValue.empty(studentId, knowledgeId));

        double currentScore = getScore(current, dimension);
        double updatedScore = currentScore + (100 - currentScore) * accuracy * 0.1;
        updatedScore = Math.min(100, updatedScore);

        AbilityValue newValue = switch (dimension) {
            case REMEMBER  -> new AbilityValue(studentId, knowledgeId, updatedScore, current.understand(), current.apply(), current.analyze(), current.evaluate(), current.create(), LocalDateTime.now());
            case UNDERSTAND -> new AbilityValue(studentId, knowledgeId, current.remember(), updatedScore, current.apply(), current.analyze(), current.evaluate(), current.create(), LocalDateTime.now());
            case APPLY     -> new AbilityValue(studentId, knowledgeId, current.remember(), current.understand(), updatedScore, current.analyze(), current.evaluate(), current.create(), LocalDateTime.now());
            case ANALYZE   -> new AbilityValue(studentId, knowledgeId, current.remember(), current.understand(), current.apply(), updatedScore, current.evaluate(), current.create(), LocalDateTime.now());
            case EVALUATE  -> new AbilityValue(studentId, knowledgeId, current.remember(), current.understand(), current.apply(), current.analyze(), updatedScore, current.create(), LocalDateTime.now());
            case CREATE    -> new AbilityValue(studentId, knowledgeId, current.remember(), current.understand(), current.apply(), current.analyze(), current.evaluate(), updatedScore, LocalDateTime.now());
        };

        store.put(key, newValue);
        log.info("能力值更新: student={}, knowledge={}, dimension={}, score={}", studentId, knowledgeId, dimension, updatedScore);
    }

    private double getScore(AbilityValue value, BloomTaxonomy dimension) {
        return switch (dimension) {
            case REMEMBER   -> value.remember();
            case UNDERSTAND -> value.understand();
            case APPLY      -> value.apply();
            case ANALYZE    -> value.analyze();
            case EVALUATE   -> value.evaluate();
            case CREATE     -> value.create();
        };
    }

    private String buildKey(Long studentId, Long knowledgeId) {
        return studentId + ":" + knowledgeId;
    }
}
