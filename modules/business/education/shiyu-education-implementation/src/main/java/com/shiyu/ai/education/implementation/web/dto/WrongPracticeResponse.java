package com.shiyu.ai.education.implementation.web.dto;

/** 当前学生尚未解决的错题，不包含其他学生的数据。 */
public record WrongPracticeResponse(Long id, Long questionId, String title,
        String lastAnswer, int correctTimes) {}
