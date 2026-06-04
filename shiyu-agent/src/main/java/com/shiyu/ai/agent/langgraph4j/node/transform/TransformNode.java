package com.shiyu.ai.agent.langgraph4j.node.transform;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.NodeFields.FieldKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据转换节点
 * 用于数据格式转换或处理
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class TransformNode extends BaseNode {

    private TransformConfig config;

    /**
     * 备选的输入字段键，按优先级从高到低排列
     */
    private static final FieldKey[] GET_INPUT_KEYS = {
            FieldKey.INPUT, FieldKey.DATA, FieldKey.CONTENT,
            FieldKey.TEXT, FieldKey.QUERY, FieldKey.USER_INPUT
    };

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private TransformNode(TransformConfig config) {
        super(config != null ? config : new TransformConfig());
        this.config = config != null ? config : new TransformConfig();
        // 设置节点类型为 TRANSFORM
        this.config.setNodeType(NodeType.TRANSFORM);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 TransformNode 实例
     */
    public static class Builder {
        private TransformConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(TransformConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 TransformNode 实例
         * @return TransformNode 实例
         */
        public TransformNode build() {
            return new TransformNode(config);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行数据转换节点：{}", config.getNodeName());
        log.debug("转换配置：transformType={}, inputFormat={}, outputFormat={}", 
                config.getTransformType(), config.getInputFormat(), config.getOutputFormat());
        
        try {
            // 1. 获取输入数据
            String inputData = getInputData(input);
            
            // 2. 根据转换类型执行转换
            Object result = transformData(inputData, input.getParameter("transformType", config.getTransformType()));
            
            // 3. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("数据转换成功");
            output.addData(FieldKey.TRANSFORMED_DATA, result);

            // 将转换后的数据也添加到 messages（标准格式）
            if (result instanceof String str) {
                output.addData(FieldKey.MESSAGES, str);
            }
            
            log.info("数据转换成功");
            return output;
            
        } catch (Exception e) {
            log.error("数据转换节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("数据转换节点执行失败：" + e.getMessage());
            return output;
        }
    }
    
    /**
     * 获取输入数据
     */
    private String getInputData(NodeInput input) {
        // 尝试多个可能的键，按优先级查找
        for (FieldKey fieldKey : GET_INPUT_KEYS) {
            Object value = input.getParameter(fieldKey, null);
            if (value != null) {
                return value.toString();
            }
        }

        return "";
    }
    
    /**
     * 执行数据转换
     */
    private Object transformData(String inputData, String transformType) {
        if (transformType == null) {
            return inputData;
        }
        
        switch (transformType) {
            case "UPPERCASE":
                return inputData.toUpperCase();
            case "LOWERCASE":
                return inputData.toLowerCase();
            case "TRIM":
                return inputData.trim();
            case "JSON_TO_MAP":
                return parseJsonToMap(inputData);
            case "MAP_TO_JSON":
                return convertMapToJson(inputData);
            case "TEMPLATE":
                return applyTemplate(inputData);
            default:
                log.warn("未知的转换类型：{}，返回原始数据", transformType);
                return inputData;
        }
    }
    
    /**
     * 解析 JSON 到 Map（简化实现）
     */
    private java.util.Map<String, Object> parseJsonToMap(String json) {
        // TODO: 实际项目应该使用 Jackson 或 Gson
        return java.util.Map.of("raw", json, "parsed", true);
    }
    
    /**
     * 转换 Map 到 JSON（简化实现）
     */
    private String convertMapToJson(String mapData) {
        // TODO: 实际项目应该使用 Jackson 或 Gson
        return "{\"data\": \"" + mapData + "\"}";
    }
    
    /**
     * 应用模板转换
     */
    private String applyTemplate(String inputData) {
        String template = config.getTemplate();
        if (template == null || template.isEmpty()) {
            return inputData;
        }
        
        // 简单的变量替换
        return template.replace("{input}", inputData);
    }
}
