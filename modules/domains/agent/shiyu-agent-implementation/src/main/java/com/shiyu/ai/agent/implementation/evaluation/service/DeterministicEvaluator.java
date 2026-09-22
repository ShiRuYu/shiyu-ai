package com.shiyu.ai.agent.implementation.evaluation.service;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalMetric;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;
import com.shiyu.ai.agent.implementation.evaluation.port.Evaluator;

import com.shiyu.ai.common.foundation.utils.JSONUtils;

import java.util.Locale;

/**
 * 实现 Deterministic Evaluator 相关的业务处理、协作逻辑或基础设施能力。
 */
public class DeterministicEvaluator implements Evaluator {
    /**
     * metric 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EvalMetric metric;

    /**
     * 执行 Deterministic Evaluator 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param metric 用于完成本次业务处理的 metric 参数。
     */
    public DeterministicEvaluator(EvalMetric metric) {
        this.metric = metric == null ? EvalMetric.EXACT_MATCH : metric;
    }

    /**
     * 执行 Deterministic Evaluator 相关业务数据，并返回处理结果。
     *
     * @param testCase 用于完成本次业务处理的 testCase 参数。
     * @param actual 用于完成本次业务处理的 actual 参数。
     * @return 返回 Deterministic Evaluator 相关操作生成的结果数据。
     */
    @Override
    public EvalResult evaluate(EvalCase testCase, String actual) {
        String expected = testCase.expected() == null ? "" : testCase.expected();
        String value = actual == null ? "" : actual;
        boolean passed =
                switch (metric) {
                    case EXACT_MATCH -> expected.trim().equals(value.trim());
                    case CONTAINS, CITATION_COVERAGE, RETRIEVAL_HIT ->
                            !expected.isBlank()
                                    && value.toLowerCase(Locale.ROOT)
                                            .contains(expected.toLowerCase(Locale.ROOT));
                    case TOKEN_BUDGET -> estimate(value) <= budget(testCase, 2000);
                    case COST_BUDGET -> estimate(value) <= budget(testCase, 100);
                    case JSON_SCHEMA -> validJson(value);
                    case TOOL_CALL_SCHEMA -> validToolCall(value, testCase);
                };
        return new EvalResult(
                testCase.id(),
                metric,
                passed ? 1D : 0D,
                passed,
                passed ? "passed" : "expected output did not satisfy metric");
    }

    private long estimate(String value) {
        return Math.max(1, value.codePointCount(0, value.length()) / 4);
    }

    private long budget(EvalCase testCase, long fallback) {
        Object value = testCase.metadata().get("budget");
        return value instanceof Number n ? n.longValue() : fallback;
    }

    private boolean validJson(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            JSONUtils.getObjectMapper().readTree(value);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean validToolCall(String value, EvalCase testCase) {
        if (!validJson(value)) return false;
        Object required = testCase.metadata().get("requiredField");
        return required == null || value.contains("\"" + required + "\"");
    }
}
