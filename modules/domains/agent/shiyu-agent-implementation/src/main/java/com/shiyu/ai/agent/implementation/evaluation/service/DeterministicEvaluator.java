package com.shiyu.ai.agent.implementation.evaluation.service;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalMetric;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;
import com.shiyu.ai.agent.implementation.evaluation.port.Evaluator;

import com.shiyu.ai.common.core.utils.JSONUtils;

import java.util.Locale;

/**
 * {@code DeterministicEvaluator} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public class DeterministicEvaluator implements Evaluator {
    /**
     * metric 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EvalMetric metric;

    /**
     * {@code DeterministicEvaluator} 创建并初始化当前类型实例。
     *
     * @param metric 参数值，用于执行当前操作。
     */
    public DeterministicEvaluator(EvalMetric metric) {
        this.metric = metric == null ? EvalMetric.EXACT_MATCH : metric;
    }

    /**
     * {@code evaluate} 执行当前类型定义的业务操作。
     *
     * @param testCase 参数值，用于执行当前操作。
     * @param actual 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
