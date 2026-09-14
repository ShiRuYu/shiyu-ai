package com.shiyu.ai.agent.implementation.evaluation.model;

/**
 * {@code EvalResult} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param caseId caseId 属性，表示该记录组件承载的数据。
 * @param metric metric 属性，表示该记录组件承载的数据。
 * @param score 分数，表示该记录组件承载的数据。
 * @param passed passed 属性，表示该记录组件承载的数据。
 * @param detail detail 属性，表示该记录组件承载的数据。
 */
public record EvalResult(
        String caseId, EvalMetric metric, double score, boolean passed, String detail) {}
