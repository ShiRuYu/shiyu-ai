package com.shiyu.ai.knowledge.implementation.domain.port.provider;

import java.util.function.Consumer;

/**
 * ChatProvider 边界接口，负责向外部组件提供知识领域相关能力。
 */
public interface ChatProvider {
    /**
     * 执行 {@code profile} 定义的接口操作。
     *
     * @return 操作结果。
     */
    String profile();

    /**
     * 执行 {@code available} 定义的接口操作。
     *
     * @return 条件是否满足。
     */
    boolean available();

    /**
     * 执行 {@code stream} 定义的接口操作。
     *
     * @param prompt 方法参数。
     * @param consumer 方法参数。
     */
    void stream(String prompt, Consumer<String> consumer);
}
