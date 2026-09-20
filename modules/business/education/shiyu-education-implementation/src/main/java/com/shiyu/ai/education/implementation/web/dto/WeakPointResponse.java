package com.shiyu.ai.education.implementation.web.dto;

/**
 * 封装 Weak Point 相关的不可变数据及其字段约束。
 */
public record WeakPointResponse(Long knowledgeId, String knowledgeName, Double mastery) {}
