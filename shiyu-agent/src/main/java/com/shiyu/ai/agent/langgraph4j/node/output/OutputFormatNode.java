package com.shiyu.ai.agent.langgraph4j.node.output;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import com.shiyu.ai.agent.langgraph4j.node.NodeFields.FieldKey;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 输出格式化节点
 * 用于格式化最终输出结果
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class OutputFormatNode extends BaseNode {

    private OutputFormatConfig config;

    /**
     * 备选的输入字段键集合，按优先级从高到低排列
     */
    private static final FieldKey[] GET_CONTENT_KEYS = {
            FieldKey.CONTENT, FieldKey.RESPONSE, FieldKey.RESULT,
            FieldKey.OUTPUT, FieldKey.ANSWER, FieldKey.MESSAGES
    };

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private OutputFormatNode(OutputFormatConfig config) {
        super(config != null ? config : new OutputFormatConfig());
        this.config = config != null ? config : new OutputFormatConfig();
        // 设置节点类型为 OUTPUT_FORMAT
        this.config.setNodeType(NodeType.OUTPUT_FORMAT);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 OutputFormatNode 实例
     */
    public static class Builder {
        private OutputFormatConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(OutputFormatConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 OutputFormatNode 实例
         * @return OutputFormatNode 实例
         */
        public OutputFormatNode build() {
            return new OutputFormatNode(config);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行输出格式化节点：{}", config.getNodeName());
        log.debug("格式化配置：outputFormat={}, prettyPrint={}, template={}", 
                config.getOutputFormat(), config.getPrettyPrint(), config.getTemplate());
        
        try {
            // 1. 获取待格式化的内容
            String content = getContent(input);
            
            // 2. 根据配置进行格式化
            String formattedContent = formatContent(content);
            
            // 3. 构建输出结果
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("输出格式化成功");
            output.addData(FieldKey.FORMATTED_CONTENT, formattedContent);
            output.addData(FieldKey.MESSAGES, formattedContent);
            
            log.info("输出格式化成功");
            return output;
            
        } catch (Exception e) {
            log.error("输出格式化节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("输出格式化节点执行失败：" + e.getMessage());
            return output;
        }
    }
    
    /**
     * 获取待格式化的内容
     */
    private String getContent(NodeInput input) {
        // 尝试多个可能的键，按优先级查找
        for (FieldKey fieldKey : GET_CONTENT_KEYS) {
            Object value = input.getParameter(fieldKey, null);
            if (value != null) {
                return value.toString();
            }
        }

        return "";
    }
    
    /**
     * 格式化内容
     */
    private String formatContent(String content) {
        String format = config.getOutputFormat();
        
        // 根据格式类型进行处理
        if (format != null) {
            switch (format) {
                case "JSON":
                    return formatAsJson(content);
                case "XML":
                    return formatAsXml(content);
                case "MARKDOWN":
                    return formatAsMarkdown(content);
                case "HTML":
                    return formatAsHtml(content);
                case "PLAIN_TEXT":
                    return content;
                default:
                    log.warn("未知的输出格式：{}，使用纯文本", format);
            }
        }
        
        // 如果有模板，使用模板
        if (config.getTemplate() != null && !config.getTemplate().isEmpty()) {
            return applyTemplate(content);
        }
        
        // 默认返回原始内容（可能进行美化）
        return config.getPrettyPrint() != null && config.getPrettyPrint() 
            ? prettifyContent(content) 
            : content;
    }
    
    /**
     * 格式化为 JSON
     */
    private String formatAsJson(String content) {
        try {
            Map<String, Object> parsed = JSONUtils.parseObject(content, Map.class);
            if (parsed != null) {
                if (config.getPrettyPrint() != null && config.getPrettyPrint()) {
                    return JSONUtils.toPrettyJsonString(parsed);
                }
                return JSONUtils.toJsonString(parsed);
            }
        } catch (Exception ignored) {
        }
        String escaped = content.replace("\"", "\\\"");
        return "{\"result\": \"" + escaped + "\"}";
    }
    
    /**
     * 格式化为 XML
     */
    private String formatAsXml(String content) {
        return "<response>\n  <content>" + content + "</content>\n</response>";
    }
    
    /**
     * 格式化为 Markdown
     */
    private String formatAsMarkdown(String content) {
        // 简单的 Markdown 格式化
        return "## 回复\n\n" + content + "\n";
    }
    
    /**
     * 格式化为 HTML
     */
    private String formatAsHtml(String content) {
        return "<div class=\"response\">\n  <p>" + content + "</p>\n</div>";
    }
    
    /**
     * 应用模板
     */
    private String applyTemplate(String content) {
        String template = config.getTemplate();
        return template.replace("{content}", content)
                      .replace("{result}", content);
    }
    
    /**
     * 美化内容
     */
    private String prettifyContent(String content) {
        // 简单的文本美化：添加适当的换行和空格
        return content.trim().replaceAll("\\s+", " ");
    }
}


