package com.shiyu.ai.agent.contract.runtime;

/**
 * 定义 Context Assembly 领域与外部能力交互的端口契约。
 */
public interface ContextAssemblyPort {
    /**
     * 获取上下文assembly。
     *
     * @param query query 参数。
     *
     * @return 处理结果。
     */
    ContextResult retrieve(ContextQuery query);

    /**
     * 封装 Context 相关的不可变数据及其字段约束。
     */
    record ContextResult(java.util.List<ContextItem> items, ContextTrace trace) {
        public ContextResult {
            items = items == null ? java.util.List.of() : java.util.List.copyOf(items);
        }
    }
}
