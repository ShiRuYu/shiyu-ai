package com.shiyu.ai.agent.implementation.evaluation.model;

/**
 * 封装 Eval 相关的不可变数据及其字段约束。
 */
public record EvalResult(
        String caseId, EvalMetric metric, double score, boolean passed, String detail) {}
