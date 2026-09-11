package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.QuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

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
        Long usedCount) {}
