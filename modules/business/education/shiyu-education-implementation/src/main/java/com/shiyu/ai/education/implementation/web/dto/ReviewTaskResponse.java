package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ReviewTaskBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.time.LocalDateTime;

/**
 * 封装 复习 Task 相关的不可变数据及其字段约束。
 */
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
        LocalDateTime completedAt) {}
