package com.shiyu.ai.agent.evaluation;

public record EvalResult(String caseId, EvalMetric metric, double score, boolean passed, String detail) { }
