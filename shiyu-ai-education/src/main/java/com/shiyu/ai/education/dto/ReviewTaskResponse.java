package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.education.bo.ReviewTaskBO;

@AutoMapper(target = ReviewTaskBO.class)
public record ReviewTaskResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String knowledgeName,
        Integer reviewRound,
        String reviewDate,
        String status,
        Double previousMastery
) {}
