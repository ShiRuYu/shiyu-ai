package com.shiyu.ai.tooling.implementation.tool.config;

import com.shiyu.ai.tooling.contract.api.ToolService;
import com.shiyu.ai.tooling.implementation.tool.ToolServiceImpl;
import com.shiyu.ai.tooling.implementation.tool.mcp.McpToolRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McpToolAutoConfigurationTest {
    @Test
    void createsRegistryAndSynchronizesBuiltinToolsOnlyForImplementation() {
        McpToolAutoConfiguration configuration = new McpToolAutoConfiguration();
        McpToolRegistry registry = configuration.mcpToolRegistry();
        ToolServiceImpl implementation = new ToolServiceImpl();
        implementation.init();
        McpToolAutoConfiguration.McpToolSyncRegistrar registrar = configuration.mcpToolSyncRegistrar(implementation, registry);
        registrar.sync();
        assertEquals(5, registry.size());

        McpToolRegistry untouched = configuration.mcpToolRegistry();
        registrar = configuration.mcpToolSyncRegistrar(new ToolService() {
            @Override public ToolExecutionResult execute(String toolName, java.util.Map<String, Object> parameters) { return new ToolExecutionResult(false, null, "not used"); }
        }, untouched);
        registrar.sync();
        assertEquals(0, untouched.size());
    }
}
