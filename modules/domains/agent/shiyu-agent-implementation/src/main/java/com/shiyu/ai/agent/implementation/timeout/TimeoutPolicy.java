package com.shiyu.ai.agent.implementation.timeout;

import java.util.concurrent.Callable;

/**
 * 校验或约束 Timeout 相关的请求、状态和访问规则。
 */
public interface TimeoutPolicy extends AutoCloseable {

    /**
     * 调用 Timeout 相关业务数据，并返回处理结果。
     *
     * @param callable 用于完成本次业务处理的 callable 参数。
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Timeout 相关操作生成的结果数据。
     */
    <T> T executeWithTimeout(Callable<T> callable, TimeoutConfig config) throws Exception;

    /**
     * 删除或移除 Timeout 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    default void close() {
    }
}
