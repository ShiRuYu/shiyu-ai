package com.shiyu.ai.education.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExamType {

    DAILY_QUIZ("DAILY_QUIZ", "随堂测验"),
    UNIT_TEST("UNIT_TEST", "单元测试"),
    MIDTERM("MIDTERM", "期中考试"),
    FINAL("FINAL", "期末考试"),
    MOCK("MOCK", "模拟考试"),
    AI_GENERATED("AI_GENERATED", "AI 组卷");

    private final String code;
    private final String name;
}
