package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

public record GenerateQuestionRequest(
        List<Long> knowledgeIds, Integer difficulty, Integer count, List<String> types) {}
