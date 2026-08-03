package com.shiyu.ai.education.dto;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.education.domain.model.QuestionBO;

@AutoMapper(target = QuestionBO.class)
public record QuestionResponse(
        Long id,
        String code,
        String type,
        String subjectCode,
        Integer grade,
        Integer difficulty,
        String abilityDimension,
        String title,
        String options,
        String answer,
        String analysis,
        String tags,
        Long usedCount
) {}
