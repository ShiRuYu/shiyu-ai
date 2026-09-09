package com.shiyu.ai.education.implementation.web.dto;

public record DailyTaskResponse(
        Long id,
        Long knowledgeId,
        String knowledgeName,
        String planDate,
        Integer status,
        String statusDesc,
        Integer orderNo
) {}

