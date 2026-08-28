package com.shiyu.ai.tool;

import java.util.Map;

/**
 * Public Tooling contract for executing a named tool.
 *
 * <p>The implementation may be backed by MCP, plugins, or local adapters;
 * callers in other bounded contexts must not depend on those details.</p>
 */
public interface ToolService {

    ToolExecutionResult execute(String toolName, Map<String, Object> parameters);

    record ToolExecutionResult(boolean success, Object result, String errorMessage) {
    }
}
