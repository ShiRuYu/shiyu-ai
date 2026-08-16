package com.shiyu.ai.evaluation;

public record EvalResult(String caseId, EvalMetric metric, double score, boolean passed, String detail) { }
