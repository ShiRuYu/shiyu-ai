package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/** 核对答案前向学生展示的试题。 */
public record PracticeQuestionResponse(Long id, String title, String type,
        String subjectCode, Integer grade, List<ExamPaperResponse.Option> options) {}
