package com.shiyu.ai.agent.langgraph4j.node.tool;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentDefinition;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.biz.agent.service.ToolService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 用于调用外部工具或服务
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class ToolCallNode extends BaseNode {

    private ToolCallConfig config;
    
    /**
     * 工具调用服务（必须依赖）
     */
    private final ToolService toolService;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     * @param toolService 工具调用服务
     */
    private ToolCallNode(ToolCallConfig config, ToolService toolService) {
        super(config != null ? config : new ToolCallConfig());
        this.config = config != null ? config : new ToolCallConfig();
        // 设置节点类型为 TOOL_CALL
        this.config.setNodeType(NodeType.TOOL_CALL);
        this.toolService = toolService;
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 ToolCallNode 实例
     */
    public static class Builder {
        private ToolCallConfig config;
        private ToolService toolService;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(ToolCallConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置工具调用服务
         * @param toolService 工具调用服务
         * @return Builder 实例
         */
        public Builder toolService(ToolService toolService) {
            this.toolService = toolService;
            return this;
        }

        /**
         * 构建并返回 ToolCallNode 实例
         * 在构建前会进行必要的校验
         * @return ToolCallNode 实例
         * @throws IllegalStateException 如果校验失败
         */
        public ToolCallNode build() {
            // 校验：toolService 不能为空
            if (toolService == null) {
                throw new IllegalStateException("创建 ToolCallNode 失败：toolService 不能为空");
            }
            
            // 所有校验通过，创建并返回实例
            return new ToolCallNode(config, toolService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行工具调用节点：{}", config.getNodeName());
        log.debug("工具配置：toolName={}, toolType={}, timeout={}", 
                config.getToolName(), config.getToolType(), config.getToolTimeout());
        
        try {
            // 1. 获取工具名称和参数
            String toolName = getToolName(input);
            
            if (toolName == null || toolName.trim().isEmpty()) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(false);
                output.setMsg("工具名称不能为空");
                return output;
            }
            
            // 2. 准备参数
            java.util.Map<String, Object> parameters = prepareToolParameters(input);
            
            // 3. 调用工具服务
            ToolService.ToolExecutionResult result = toolService.execute(toolName, parameters);
            
            // 4. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(result.success());
            output.setMsg(result.errorMessage() != null ? result.errorMessage() : "工具调用成功");
            
            if (result.success()) {
                output.addData(FieldKey.TOOL_NAME, toolName);
                output.addData(FieldKey.TOOL_RESULT, result.result());
                log.info("工具调用成功：{}", toolName);
            } else {
                log.error("工具调用失败：{}", result.errorMessage());
            }
            
            log.info("工具调用节点执行完成");
            return output;
            
        } catch (Exception e) {
            log.error("工具调用节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("工具调用节点执行失败：" + e.getMessage());
            return output;
        }
    }
    
    /**
     * 获取工具名称
     * <p>
     * 优先级: input (runtime) > config > IntentDefinitionFactory (从 intentCode 推导)
     */
    private String getToolName(NodeInput input) {
        // 1. Input（最高优先级）
        String toolName = input.getParameter(FieldKey.TOOL_NAME, "");
        if (toolName != null && !toolName.trim().isEmpty()) {
            return toolName;
        }
        
        // 2. Config（次高优先级）
        if (config.getToolName() != null && !config.getToolName().trim().isEmpty()) {
            return config.getToolName();
        }

        // 3. Factory（最低优先级）— 从 intentCode 查找对应 IntentDefinition 的 toolName
        String intentCode = input.getParameter(FieldKey.INTENT_CODE, "");
        if (intentCode != null && !intentCode.trim().isEmpty()) {
            for (String agentId : IntentDefinitionFactory.getAgentIds()) {
                List<IntentDefinition> defs = IntentDefinitionFactory.getAll(agentId);
                for (IntentDefinition def : defs) {
                    if (intentCode.equals(def.getCode())
                            && def.getToolName() != null && !def.getToolName().trim().isEmpty()) {
                        log.debug("从 IntentDefinition 推导 toolName: {} (intentCode={})",
                                def.getToolName(), intentCode);
                        return def.getToolName();
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 准备工具参数
     * <p>
     * 从输入中提取工具参数：排除 {@link FieldKey#TOOL_NAME} 和 {@link FieldKey#TOOL_TYPE}，
     * 并将 {@link FieldKey#SLOTS} 展开为独立字段。
     */
    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> prepareToolParameters(NodeInput input) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        
        for (java.util.Map.Entry<String, Object> entry : input.toMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 排除元数据字段
            if (FieldKey.TOOL_NAME.key().equals(key)
                    || FieldKey.TOOL_TYPE.key().equals(key)
                    || FieldKey.INTENT_CODE.key().equals(key)
                    || FieldKey.INTENT_NAME.key().equals(key)
                    || FieldKey.CONFIDENCE.key().equals(key)
                    || key.startsWith("_")) {
                continue;
            }
            
            // 展平 slots 为独立字段
            if (FieldKey.SLOTS.key().equals(key) && value instanceof java.util.Map) {
                java.util.Map<String, Object> slots = (java.util.Map<String, Object>) value;
                params.putAll(slots);
                continue;
            }
            
            params.put(key, value);
        }
        
        return params;
    }
}
