package com.shiyu.ai.agent.langgraph4j.node.tool;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.biz.agent.service.ToolService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
     * 从配置中读取 toolName，每个 ToolCallNode 实例只对应一个工具。
     */
    private String getToolName(NodeInput input) {
        return config.getToolName();
    }
    
    /**
     * 准备工具参数
     * <p>
     * 从 input 中提取参数并处理：
     * <ol>
     *   <li>提取原始参数，排除元数据字段</li>
     *   <li>将 {@link FieldKey#SLOTS} 展平为独立字段</li>
     *   <li>通过 {@link FieldKey#PARAMETER_MAPPING} 重命名 slot key</li>
     *   <li>通过 {@link FieldKey#SLOT_DEFAULTS} 补充缺失的默认值</li>
     *   <li>通过 {@link FieldKey#SLOT_DEFINITIONS} 校验部分 slot 缺失</li>
     * </ol>
     * <p>
     * 参数映射/默认值/schema 均由上游 {@code IntentNode} 从 {@code IntentDefinition} 解析后
     * 写入 state 传递至此，不再直接查询工厂。
     */
    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> prepareToolParameters(NodeInput input) {
        // 1. 从 state 读取上游 IntentNode 传递的配置
        java.util.Map<String, String> parameterMapping = input.getParameter(FieldKey.PARAMETER_MAPPING, null);
        java.util.Map<String, String> slotDefaults = input.getParameter(FieldKey.SLOT_DEFAULTS, null);
        java.util.Map<String, String> slotDefinitions = input.getParameter(FieldKey.SLOT_DEFINITIONS, null);

        // 2. 收集原始参数，排除元数据
        java.util.Map<String, Object> raw = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Object> entry : input.toMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 排除元数据字段
            if (FieldKey.TOOL_NAME.key().equals(key)
                    || FieldKey.TOOL_TYPE.key().equals(key)
                    || FieldKey.INTENT_CODE.key().equals(key)
                    || FieldKey.INTENT_NAME.key().equals(key)
                    || FieldKey.CONFIDENCE.key().equals(key)
                    || FieldKey.PARAMETER_MAPPING.key().equals(key)
                    || FieldKey.SLOT_DEFAULTS.key().equals(key)
                    || FieldKey.SLOT_DEFINITIONS.key().equals(key)
                    || key.startsWith("_")) {
                continue;
            }

            // 展平 slots 为独立字段
            if (FieldKey.SLOTS.key().equals(key) && value instanceof java.util.Map) {
                java.util.Map<String, Object> slots = (java.util.Map<String, Object>) value;
                raw.putAll(slots);
                continue;
            }

            raw.put(key, value);
        }

        // 3. 参数重命名（slot名 → 工具参数名）
        if (parameterMapping != null && !parameterMapping.isEmpty()) {
            for (java.util.Map.Entry<String, String> me : parameterMapping.entrySet()) {
                String slotName = me.getKey();
                String paramName = me.getValue();
                if (raw.containsKey(slotName) && !slotName.equals(paramName)) {
                    raw.put(paramName, raw.remove(slotName));
                }
            }
        }

        // 4. 补充 slot 默认值（仅当缺失时）
        if (slotDefaults != null && !slotDefaults.isEmpty()) {
            for (java.util.Map.Entry<String, String> de : slotDefaults.entrySet()) {
                String slotName = de.getKey();
                String effectiveKey = parameterMapping != null
                        ? parameterMapping.getOrDefault(slotName, slotName)
                        : slotName;
                if (!raw.containsKey(effectiveKey)) {
                    raw.put(effectiveKey, de.getValue());
                    log.debug("补充 slot 默认值: {}={}", effectiveKey, de.getValue());
                }
            }
        }

        // 5. 校验部分 slot 缺失（仅 warn，不阻断）
        if (slotDefinitions != null && !slotDefinitions.isEmpty()) {
            for (String slotName : slotDefinitions.keySet()) {
                String effectiveKey = parameterMapping != null
                        ? parameterMapping.getOrDefault(slotName, slotName)
                        : slotName;
                if (!raw.containsKey(effectiveKey) || raw.get(effectiveKey) == null
                        || "".equals(raw.get(effectiveKey).toString().trim())) {
                    log.warn("部分 slot 缺失: {} (原始slot名={})", effectiveKey, slotName);
                }
            }
        }

        return raw;
    }
}
