package com.shiyu.ai.tool.mcp.config;

import com.shiyu.ai.tool.ToolService;
import com.shiyu.ai.tool.impl.ToolServiceImpl;
import java.util.stream.Collectors;
import com.shiyu.ai.tool.mcp.McpToolDescriptor;
import com.shiyu.ai.tool.mcp.McpToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP 工具市场自动配置
 * 将 ToolServiceImpl 中已有的工具注册到 McpToolRegistry
 */
@Slf4j
@Configuration
public class McpToolAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public McpToolRegistry mcpToolRegistry() {
        return new McpToolRegistry();
    }

    /**
     * 将 ToolServiceImpl 内置工具同步注册到 McpToolRegistry
     */
    @Bean
    public Object mcpToolSyncRegistrar(ToolService toolService, McpToolRegistry registry) {
        if (toolService instanceof ToolServiceImpl impl) {
            List<ToolServiceImpl.ToolDefinition> tools = impl.listTools();
            List<McpToolDescriptor> descriptors = tools.stream()
                    .map(t -> new McpToolDescriptor(
                            t.name(),
                            t.description(),
                            "builtin",
                            t.parameters().entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> new McpToolDescriptor.ParameterInfo(
                                                    e.getValue().type(),
                                                    e.getValue().description(),
                                                    e.getValue().required()
                                            )
                                    )),
                            List.of(t.builtin() ? "builtin" : "custom"),
                            "builtin",
                            t.builtin()
                    ))
                    .collect(Collectors.toList());

            registry.registerAll(descriptors);
            log.info("已同步 {} 个内置工具到 MCP 工具市场", descriptors.size());
        }
        return registry;
    }
}
