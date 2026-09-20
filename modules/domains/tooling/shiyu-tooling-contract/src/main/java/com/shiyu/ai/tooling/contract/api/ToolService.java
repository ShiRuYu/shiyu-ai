package com.shiyu.ai.tooling.contract.api;

import java.util.Map;

/**
 * 提供 工具 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface ToolService {

    /**
     * 调用 工具 相关业务数据，并返回处理结果。
     *
     * @param toolName 用于完成本次业务处理的 toolName 参数。
     * @param parameters 用于完成本次业务处理的 parameters 参数。
     * @return 返回 工具 相关操作生成的结果数据。
     */
    ToolExecutionResult execute(String toolName, Map<String, Object> parameters);

    /**
     * 封装 工具 Execution 相关的不可变数据及其字段约束。
     */
    record ToolExecutionResult(boolean success, Object result, String errorMessage) {}
}
