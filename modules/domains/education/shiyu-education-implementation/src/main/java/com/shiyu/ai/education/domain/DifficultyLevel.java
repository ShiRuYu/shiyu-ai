package com.shiyu.ai.education.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DifficultyLevel {

    BASIC(1, "基础", 0.40),
    MEDIUM(2, "中等", 0.40),
    HARD(3, "困难", 0.15),
    COMPETITION(4, "竞赛", 0.05);

    private final int level;
    private final String name;
    private final double ratio;

    public static DifficultyLevel fromLevel(int level) {
        for (DifficultyLevel d : values()) {
            if (d.level == level) return d;
        }
        throw new IllegalArgumentException("Invalid difficulty level: " + level);
    }
}
