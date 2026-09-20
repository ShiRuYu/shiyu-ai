package com.shiyu.ai.knowledge.implementation.domain.port.provider;

import java.util.function.Consumer;

/**
 * 创建或提供 对话 相关的业务组件和运行时能力。
 */
public interface ChatProvider {
    /**
     * 执行 对话 相关业务数据，并返回处理结果。
     *
     * @return 返回 对话 相关操作生成的结果数据。
     */
    String profile();

    /**
     * 执行 对话 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    boolean available();

    /**
     * 执行 对话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param prompt 用于完成本次业务处理的 prompt 参数。
     * @param consumer 用于完成本次业务处理的 consumer 参数。
     */
    void stream(String prompt, Consumer<String> consumer);
}
