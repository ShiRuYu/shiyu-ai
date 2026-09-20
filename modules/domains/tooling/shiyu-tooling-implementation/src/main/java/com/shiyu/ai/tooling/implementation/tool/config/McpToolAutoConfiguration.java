package com.shiyu.ai.tooling.implementation.tool.config;

import com.shiyu.ai.tooling.contract.api.ToolService;
import com.shiyu.ai.tooling.implementation.tool.ToolServiceImpl;
import com.shiyu.ai.tooling.implementation.tool.mcp.McpToolDescriptor;
import com.shiyu.ai.tooling.implementation.tool.mcp.McpToolRegistry;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 Mcp 工具 Auto 基础设施或应用能力的配置项及装配规则。
 */
@Slf4j
@Configuration
public class McpToolAutoConfiguration {

    /**
     * 执行 Mcp 工具 Auto 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mcp 工具 Auto 相关操作生成的结果数据。
     */
    @Bean
    @ConditionalOnMissingBean
    public McpToolRegistry mcpToolRegistry() {
        return new McpToolRegistry();
    }

    /** 将 ToolServiceImpl 内置工具同步注册到 McpToolRegistry */
    @Bean
    public McpToolSyncRegistrar mcpToolSyncRegistrar(
            ToolService toolService, McpToolRegistry registry) {
        return new McpToolSyncRegistrar(toolService, registry);
    }

    /**
     * 实现 Mcp 工具 Sync Registrar 相关的业务处理、协作逻辑或基础设施能力。
     */
    @Slf4j
    public static class McpToolSyncRegistrar {

        private final ToolService toolService;
        /**
         * registry 属性，保存当前对象中的业务数据或协作依赖。
         */
        private final McpToolRegistry registry;

        McpToolSyncRegistrar(ToolService toolService, McpToolRegistry registry) {
            this.toolService = toolService;
            this.registry = registry;
        }

        /**
         * 执行 Mcp 工具 Sync Registrar 相关业务操作，并维护必要的状态和协作关系。
         */
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
