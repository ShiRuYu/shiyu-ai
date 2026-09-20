package com.shiyu.ai.agent.implementation.evaluation.model;

/**
 * 定义 Eval Metric 可用的枚举值及其业务语义。
 */
public enum EvalMetric {
    EXACT_MATCH,
    CONTAINS,
    JSON_SCHEMA,
    TOOL_CALL_SCHEMA,
    CITATION_COVERAGE,
    RETRIEVAL_HIT,
    TOKEN_BUDGET,
    COST_BUDGET
}
