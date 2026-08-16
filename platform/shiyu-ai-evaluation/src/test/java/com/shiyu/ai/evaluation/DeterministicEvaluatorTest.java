package com.shiyu.ai.evaluation;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class DeterministicEvaluatorTest {
    @Test void exactAndContainsMetricsAreDeterministic() {
        EvalCase c = new EvalCase("c", "d", 1, "question", "answer", Map.of(), null);
        assertThat(new DeterministicEvaluator(EvalMetric.EXACT_MATCH).evaluate(c, "answer").passed()).isTrue();
        assertThat(new DeterministicEvaluator(EvalMetric.CONTAINS).evaluate(c, "an answer here").passed()).isTrue();
    }

    @Test void jsonMetricsRejectMalformedPayloadsAndHonorToolField() {
        EvalCase json = new EvalCase("json", "d", 1, "question", "", Map.of(), null);
        DeterministicEvaluator evaluator = new DeterministicEvaluator(EvalMetric.JSON_SCHEMA);
        assertThat(evaluator.evaluate(json, "{\"ok\":true}").passed()).isTrue();
        assertThat(evaluator.evaluate(json, "{broken").passed()).isFalse();
        EvalCase tool = new EvalCase("tool", "d", 1, "question", "", Map.of("requiredField", "name"), null);
        assertThat(new DeterministicEvaluator(EvalMetric.TOOL_CALL_SCHEMA).evaluate(tool, "{\"name\":\"search\"}").passed()).isTrue();
        assertThat(new DeterministicEvaluator(EvalMetric.TOOL_CALL_SCHEMA).evaluate(tool, "{\"arguments\":{}}").passed()).isFalse();
    }
}
