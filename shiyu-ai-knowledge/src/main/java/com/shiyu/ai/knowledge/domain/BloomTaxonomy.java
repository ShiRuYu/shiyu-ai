package com.shiyu.ai.knowledge.domain;

import lombok.Getter;

@Getter
public enum BloomTaxonomy {

    REMEMBER("记忆", 1, 0.15),
    UNDERSTAND("理解", 2, 0.20),
    APPLY("应用", 3, 0.25),
    ANALYZE("分析", 4, 0.20),
    EVALUATE("评价", 5, 0.10),
    CREATE("创造", 6, 0.10);

    private final String label;
    private final int level;
    private final double weight;

    BloomTaxonomy(String label, int level, double weight) {
        this.label = label;
        this.level = level;
        this.weight = weight;
    }
}
