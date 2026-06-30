package com.shiyu.ai.education.dto;

public record DailyTaskResponse(
        Long id,
        Long knowledgeId,
        String knowledgeName,
        String planDate,
        String status,
        Integer orderNo
) {}
