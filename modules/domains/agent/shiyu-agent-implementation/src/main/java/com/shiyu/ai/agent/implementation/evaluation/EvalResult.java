package com.shiyu.ai.agent.implementation.evaluation;

public record EvalResult(String caseId, EvalMetric metric, double score, boolean passed, String detail) { }
