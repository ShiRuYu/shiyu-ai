package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.QuestionBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 封装 题目 相关的不可变数据及其字段约束。
 */
@AutoMapper(target = QuestionBO.class)
public record QuestionResponse(
        Long id,
        String code,
        String type,
        @Schema(allowableValues = {"PRACTICE", "EXAM_PRIVATE"}) String purpose,
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
