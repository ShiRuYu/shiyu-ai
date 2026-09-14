package com.shiyu.ai.education.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code ExamType} 表示教育模块中的一组受控业务状态或分类。
 */
@Getter
@AllArgsConstructor
public enum ExamType {
    DAILY_QUIZ("DAILY_QUIZ", "随堂测验"),
    UNIT_TEST("UNIT_TEST", "单元测试"),
    MIDTERM("MIDTERM", "期中考试"),
    FINAL("FINAL", "期末考试"),
    MOCK("MOCK", "模拟考试"),
    AI_GENERATED("AI_GENERATED", "AI 组卷");

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
}
