package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 Daily Task 相关的不可变数据及其字段约束。
 */
public record DailyTaskResponse(
        Long id,
        Long knowledgeId,
        String knowledgeName,
        String planDate,
        Integer status,
        String statusDesc,
        Integer orderNo) {}
