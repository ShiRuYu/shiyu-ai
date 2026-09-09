package com.shiyu.ai.education.implementation.web.dto;

import java.time.LocalDateTime;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.implementation.domain.model.ReviewTaskBO;

@AutoMapper(target = ReviewTaskBO.class)
public record ReviewTaskResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String knowledgeName,
        Integer reviewRound,
        String reviewDate,
        Integer status,
        String statusDesc,
        Double resultScore,
        LocalDateTime completedAt
) {}

