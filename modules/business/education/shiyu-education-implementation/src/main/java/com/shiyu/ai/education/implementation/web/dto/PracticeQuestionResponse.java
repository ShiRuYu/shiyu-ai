package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/** Question shown to a student before an answer is checked. */
public record PracticeQuestionResponse(Long id, String title, String type,
        String subjectCode, Integer grade, List<ExamPaperResponse.Option> options) {}
