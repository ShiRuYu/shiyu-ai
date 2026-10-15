package com.shiyu.ai.education.implementation.web.dto;

/** 学生提交一条练习答案后的即时反馈。 */
public record PracticeCheckResponse(Long questionId, boolean correct,
        String correctAnswer, String analysis) {}
