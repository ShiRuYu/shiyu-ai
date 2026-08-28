package com.shiyu.ai.agent.evaluation;

import com.shiyu.ai.common.core.utils.JSONUtils;
import java.util.Locale;

public class DeterministicEvaluator implements Evaluator {
    private final EvalMetric metric;
    public DeterministicEvaluator(EvalMetric metric) { this.metric = metric == null ? EvalMetric.EXACT_MATCH : metric; }
    @Override public EvalResult evaluate(EvalCase testCase, String actual) {
        String expected = testCase.expected() == null ? "" : testCase.expected();
        String value = actual == null ? "" : actual;
        boolean passed = switch (metric) {
            case EXACT_MATCH -> expected.trim().equals(value.trim());
            case CONTAINS, CITATION_COVERAGE, RETRIEVAL_HIT -> !expected.isBlank() && value.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
            case TOKEN_BUDGET -> estimate(value) <= budget(testCase, 2000);
            case COST_BUDGET -> estimate(value) <= budget(testCase, 100);
            case JSON_SCHEMA -> validJson(value);
            case TOOL_CALL_SCHEMA -> validToolCall(value, testCase);
        };
        return new EvalResult(testCase.id(), metric, passed ? 1D : 0D, passed, passed ? "passed" : "expected output did not satisfy metric");
    }
    private long estimate(String value) { return Math.max(1, value.codePointCount(0, value.length()) / 4); }
    private long budget(EvalCase testCase, long fallback) { Object value = testCase.metadata().get("budget"); return value instanceof Number n ? n.longValue() : fallback; }
    private boolean validJson(String value) {
        if (value == null || value.isBlank()) return false;
        try { JSONUtils.getObjectMapper().readTree(value); return true; } catch (RuntimeException ex) { return false; }
    }
    private boolean validToolCall(String value, EvalCase testCase) {
        if (!validJson(value)) return false;
        Object required = testCase.metadata().get("requiredField");
        return required == null || value.contains("\"" + required + "\"");
    }
}
