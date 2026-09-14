package com.shiyu.ai.agent.implementation.evaluation.model;

/**
 * {@code EvalMetric} 表示智能体模块中的一组受控业务状态或分类。
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
