package com.shiyu.ai.agent.config;

import com.shiyu.ai.agent.builder.AgentBuilder;
import com.shiyu.ai.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.node.DefaultNode;
import com.shiyu.ai.agent.node.llm.LlmCallNode;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.agent.service.Lc4jService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent 启动配置
 * 在应用启动时自动创建示例 Agent
 */
@Slf4j
@Component
public class AgentStartupConfig implements ApplicationRunner {

    private final AgentService agentService;
    private final Lc4jService lc4jService;

    public AgentStartupConfig(AgentService agentService, Lc4jService lc4jService) {
        this.agentService = agentService;
        this.lc4jService = lc4jService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化示例 Agent...");
        
        try {
            // 创建示例 Agent：智能助手
            createSimpleAssistantAgent();
            
            // 创建示例 Agent：客服问答助手
            createCustomerServiceAgent();
            
            log.info("示例 Agent 初始化完成");
            
        } catch (Exception e) {
            log.error("示例 Agent 初始化失败", e);
        }
    }

    /**
     * 创建简单助手 Agent（最简单的单节点 Agent）
     */
    private void createSimpleAssistantAgent() {
        log.info("创建示例 Agent：simple-assistant");
        
        try {
            // 使用 AgentBuilder 构建 Agent
            AgentBuilder builder = new AgentBuilder();
            
            AgentDefinition agent = builder
                    .agentId("simple-assistant")
                    .name("简单助手")
                    .description("一个简单的单节点助手 Agent")
                    .version("v1.0.0")
                    .versionDescription("初始版本")
                    
                    // 添加一个默认节点（简单处理输入并返回）
                    .addNode("default", DefaultNode.builder().build())
                    
                    // 设置起始和结束节点
                    .setStartNode("default")
                    .setEndNode("default")
                    
                    // 构建并注册
                    .buildAndRegister(agentService);
            
            log.info("示例 Agent 创建成功：agentId={}, name={}", 
                    agent.getAgentId(), agent.getName());
            
        } catch (Exception e) {
            log.error("创建示例 Agent 失败：simple-assistant", e);
        }
    }

    /**
     * 创建客服问答 Agent（多节点流程）
     */
    private void createCustomerServiceAgent() {
        log.info("创建示例 Agent：customer-service-agent");
        
        try {
            // 使用 AgentBuilder 构建 Agent
            AgentBuilder builder = new AgentBuilder();
            
            // 创建节点实例
            DefaultNode inputNode = DefaultNode.builder().build();
            LlmCallNode llmNode = LlmCallNode.builder()
                    .lc4jService(lc4jService)
                    .build();
            DefaultNode outputNode = DefaultNode.builder().build();
            
            AgentDefinition agent = builder
                    .agentId("customer-service-agent")
                    .name("客服问答助手")
                    .description("智能客服问答助手，处理用户咨询")
                    .version("v1.0.0")
                    .versionDescription("初始版本 - 支持基础问答流程")
                    
                    // 添加节点
                    .addNode("input", inputNode)
                    .addNode("llm", llmNode)
                    .addNode("output", outputNode)
                    
                    // 添加边（定义执行顺序）
                    .addEdge("input", "llm")
                    .addEdge("llm", "output")
                    
                    // 设置起始和结束节点
                    .setStartNode("input")
                    .setEndNode("output")
                    
                    // 构建并注册
                    .buildAndRegister(agentService);
            
            log.info("示例 Agent 创建成功：agentId={}, name={}", 
                    agent.getAgentId(), agent.getName());
            
        } catch (Exception e) {
            log.error("创建示例 Agent 失败：customer-service-agent", e);
        }
    }

    /**
     * 创建带条件分支的 Agent 示例（高级用法）
     */
    private void createConditionalAgent() {
        log.info("创建示例 Agent：conditional-agent");
        
        try {
            AgentBuilder builder = new AgentBuilder();
            
            // 创建节点
            DefaultNode startNode = DefaultNode.builder().build();
            DefaultNode processNode = DefaultNode.builder().build();
            DefaultNode fallbackNode = DefaultNode.builder().build();
            DefaultNode endNode = DefaultNode.builder().build();
            
            AgentDefinition agent = builder
                    .agentId("conditional-agent")
                    .name("条件分支 Agent")
                    .description("演示条件分支的高级 Agent")
                    .version("v1.0.0")
                    
                    // 添加节点
                    .addNode("start", startNode)
                    .addNode("process", processNode)
                    .addNode("fallback", fallbackNode)
                    .addNode("end", endNode)
                    
                    // 添加普通边
                    .addEdge("start", "process")
                    .addEdge("end", "end")
                    
                    // 添加条件边（根据处理结果决定下一步）
                    .addConditionalEdge("process",
                            "end", // 默认目标
                            Map.of(
                                // 如果返回 true，走 success 分支
                                (Map<String, Object> state) -> {
                                    Boolean success = (Boolean) state.get("success");
                                    return success != null && success;
                                },
                                "end",
                                // 如果返回 false，走 fallback 分支
                                (Map<String, Object> state) -> {
                                    Boolean success = (Boolean) state.get("success");
                                    return success == null || !success;
                                },
                                "fallback"
                            )
                    )
                    
                    // fallback 节点完成后也到结束
                    .addEdge("fallback", "end")
                    
                    // 设置起始节点
                    .setStartNode("start")
                    .setEndNode("end")
                    
                    // 构建并注册
                    .buildAndRegister(agentService);
            
            log.info("示例 Agent 创建成功：agentId={}, name={}", 
                    agent.getAgentId(), agent.getName());
            
        } catch (Exception e) {
            log.error("创建示例 Agent 失败：conditional-agent", e);
        }
    }
}
