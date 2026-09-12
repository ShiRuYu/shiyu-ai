package com.shiyu.ai.tooling.contract.api;

import java.util.Map;

/**
 * ToolService 服务接口，负责执行工具领域相关业务操作。
 */
public interface ToolService {

    /**
     * 执行当前接口定义的业务流程。
     *
     * @param toolName 方法参数。
     * @param parameters 方法参数。
     *
     * @return 操作结果。
     */
    ToolExecutionResult execute(String toolName, Map<String, Object> parameters);

    /**
     * {@code ToolExecutionResult} 封装工具模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param success success 属性，表示该记录组件承载的数据。
     * @param result 结果，表示该记录组件承载的数据。
     * @param errorMessage errorMessage 属性，表示该记录组件承载的数据。
     */
    record ToolExecutionResult(boolean success, Object result, String errorMessage) {}
}
