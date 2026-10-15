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
     * 在执行上下文中调用工具。旧实现默认忽略上下文；需要外部幂等或状态核查的适配器应显式实现此方法。
     *
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @param context 本次工具执行上下文
     * @return 工具执行结果
     */
    default ToolExecutionResult execute(
            String toolName, Map<String, Object> parameters, ToolExecutionContext context) {
        return execute(toolName, parameters);
    }

    /** 外部工具或未知工具需要审批，除非具体实现能证明其安全。 */
    default boolean requiresApproval(String toolName) {
        return true;
    }

    /** 稳定标识一次经审批的工具操作，供适配器向远端传递幂等键或查询执行状态。 */
    record ToolExecutionContext(String operationId) {}

    /**
     * 封装 工具 Execution 相关的不可变数据及其字段约束。
     */
    record ToolExecutionResult(boolean success, Object result, String errorMessage) {}
}
