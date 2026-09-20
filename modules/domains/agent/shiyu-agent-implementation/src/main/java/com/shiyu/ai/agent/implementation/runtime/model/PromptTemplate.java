package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;

import java.time.Instant;
import java.util.List;

/**
 * 封装 提示词 Template 相关的不可变数据及其字段约束。
 */
public record PromptTemplate(
        String id,
        long tenantId,
        long ownerUserId,
        String name,
        String template,
        List<String> variables,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
