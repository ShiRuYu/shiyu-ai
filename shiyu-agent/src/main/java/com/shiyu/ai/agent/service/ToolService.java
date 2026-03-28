package com.shiyu.ai.agent.service;

import java.util.Map;

/**
 * 工具调用服务接口
 * 用于调用外部工具或服务
 */
public interface ToolService {
    
    /**
     * 执行工具
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    ToolExecutionResult execute(String toolName, Map<String, Object> parameters);
    
    /**
     * 工具执行结果
     */
    record ToolExecutionResult(
        boolean success,
        Object result,
        String errorMessage
    ) {}
}
