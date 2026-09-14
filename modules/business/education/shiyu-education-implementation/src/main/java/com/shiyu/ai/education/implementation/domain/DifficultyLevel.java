package com.shiyu.ai.education.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code DifficultyLevel} 表示教育模块中的一组受控业务状态或分类。
 */
@Getter
@AllArgsConstructor
public enum DifficultyLevel {
    BASIC(1, "基础", 0.40),
    MEDIUM(2, "中等", 0.40),
    HARD(3, "困难", 0.15),
    COMPETITION(4, "竞赛", 0.05);

    /**
     * 级别，表示当前对象中的对应属性。
     */
    private final int level;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
    /**
     * 比例，表示当前对象中的对应属性。
     */
    private final double ratio;

    /**
     * {@code fromLevel} 执行当前类型定义的业务操作。
     *
     * @param level 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static DifficultyLevel fromLevel(int level) {
        for (DifficultyLevel d : values()) {
            if (d.level == level) return d;
        }
        throw new IllegalArgumentException("Invalid difficulty level: " + level);
    }
}
