package com.shiyu.ai.agent.implementation.evaluation.port;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;

/**
 * Evaluator 接口，定义智能体模块的能力边界。
 */
public interface Evaluator {
    /**
     * 执行 {@code evaluate} 定义的接口操作。
     *
     * @param testCase 方法参数。
     * @param actual 方法参数。
     *
     * @return 操作结果。
     */
    EvalResult evaluate(EvalCase testCase, String actual);
}
