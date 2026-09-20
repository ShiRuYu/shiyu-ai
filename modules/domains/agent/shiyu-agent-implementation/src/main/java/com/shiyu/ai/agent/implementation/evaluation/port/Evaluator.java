package com.shiyu.ai.agent.implementation.evaluation.port;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;

/**
 * 定义 Evaluator 相关的协作契约和调用边界。
 */
public interface Evaluator {
    /**
     * 执行 Evaluator 相关业务数据，并返回处理结果。
     *
     * @param testCase 用于完成本次业务处理的 testCase 参数。
     * @param actual 用于完成本次业务处理的 actual 参数。
     * @return 返回 Evaluator 相关操作生成的结果数据。
     */
    EvalResult evaluate(EvalCase testCase, String actual);
}
