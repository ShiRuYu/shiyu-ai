package com.shiyu.ai.tool.mcp.config;

import com.shiyu.ai.tool.ToolService;
import com.shiyu.ai.tool.impl.ToolServiceImpl;
import com.shiyu.ai.tool.mcp.McpToolDescriptor;
import com.shiyu.ai.tool.mcp.McpToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public McpToolSyncRegistrar mcpToolSyncRegistrar(ToolService toolService, McpToolRegistry registry) {
        return new McpToolSyncRegistrar(toolService, registry);
    }

    /**
     * 同步注册器
     */
    @Slf4j
    public static class McpToolSyncRegistrar {

        private final ToolService toolService;
        private final McpToolRegistry registry;

        McpToolSyncRegistrar(ToolService toolService, McpToolRegistry registry) {
            this.toolService = toolService;
            this.registry = registry;
        }

        @PostConstruct
        public void sync() {
            if (toolService instanceof ToolServiceImpl impl) {
                java.util.List<McpToolDescriptor> descriptors = impl.listToolDescriptors();
                registry.registerAll(descriptors);
                log.info("已同步 {} 个内置工具到 MCP 工具市场", descriptors.size());
            } else {
                log.warn("ToolService 不是 ToolServiceImpl 实例，跳过同步");
            }
        }
    }
}
