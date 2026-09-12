package com.shiyu.ai.agent.contract.runtime;

/**
 * ContextAssemblyPort 边界接口，负责向外部组件提供智能体领域相关能力。
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
     * 封装 Context 处理结果及追踪信息。
     * @param items items 属性，表示该记录组件承载的数据。
     * @param trace trace 属性，表示该记录组件承载的数据。
     */
    record ContextResult(java.util.List<ContextItem> items, ContextTrace trace) {
        public ContextResult {
            items = items == null ? java.util.List.of() : java.util.List.copyOf(items);
        }
    }
}
