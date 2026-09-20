package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/**
 * 封装 Generate 题目 相关的不可变数据及其字段约束。
 */
public record GenerateQuestionRequest(
        List<Long> knowledgeIds, Integer difficulty, Integer count, List<String> types) {}
