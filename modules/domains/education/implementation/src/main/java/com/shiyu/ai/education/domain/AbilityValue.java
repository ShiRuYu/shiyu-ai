package com.shiyu.ai.education.domain;

import java.time.LocalDateTime;

public record AbilityValue(
        Long studentId,
        Long knowledgeId,
        double remember,
        double understand,
        double apply,
        double analyze,
        double evaluate,
        double create,
        LocalDateTime lastUpdated
) {

    public double overallScore() {
        return remember * BloomTaxonomy.REMEMBER.getWeight()
                + understand * BloomTaxonomy.UNDERSTAND.getWeight()
                + apply * BloomTaxonomy.APPLY.getWeight()
                + analyze * BloomTaxonomy.ANALYZE.getWeight()
                + evaluate * BloomTaxonomy.EVALUATE.getWeight()
                + create * BloomTaxonomy.CREATE.getWeight();
    }

    public static AbilityValue empty(Long studentId, Long knowledgeId) {
        return new AbilityValue(studentId, knowledgeId, 0, 0, 0, 0, 0, 0, LocalDateTime.now());
    }
}
