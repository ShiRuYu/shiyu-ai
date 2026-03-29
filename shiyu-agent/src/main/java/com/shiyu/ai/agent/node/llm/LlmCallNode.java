package com.shiyu.ai.agent.node.llm;

import com.shiyu.ai.agent.domain.ChatType;
import com.shiyu.ai.agent.domain.Lc4jRequest;
import com.shiyu.ai.agent.domain.Lc4jResponse;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.service.Lc4jService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.langchain4j.generators.StreamingChatGenerator;
import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;

/**
 * LLM 调用节点
 * 用于调用大语言模型生成回复
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class LlmCallNode extends BaseNode {

    private LlmCallConfig config;
    
    /**
     * LLM 服务（必须依赖）
     */
    private final Lc4jService lc4jService;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     * @param lc4jService LLM 服务
     */
    private LlmCallNode(LlmCallConfig config, Lc4jService lc4jService) {
        super(config != null ? config : new LlmCallConfig());
        this.config = config != null ? config : new LlmCallConfig();
        // 设置节点类型为 LLM_CALL
        this.config.setNodeType(NodeType.LLM_CALL);
        this.lc4jService = lc4jService;
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 LlmCallNode 实例
     */
    public static class Builder {
        private LlmCallConfig config;
        private Lc4jService lc4jService;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(LlmCallConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置 LLM 服务
         * @param lc4jService LLM 服务
         * @return Builder 实例
         */
        public Builder lc4jService(Lc4jService lc4jService) {
            this.lc4jService = lc4jService;
            return this;
        }

        /**
         * 构建并返回 LlmCallNode 实例
         * 在构建前会进行必要的校验
         * @return LlmCallNode 实例
         * @throws IllegalStateException 如果校验失败
         */
        public LlmCallNode build() {
            // 校验：lc4jService 不能为空
            if (lc4jService == null) {
                throw new IllegalStateException("创建 LlmCallNode 失败：lc4jService 不能为空");
            }
            
            // 所有校验通过，创建并返回实例
            return new LlmCallNode(config, lc4jService);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 LLM 调用节点：{}", config.getNodeName());
        log.debug("LLM 配置：modelName={}, temperature={}, maxTokens={}, stream={}", 
                config.getModelName(), config.getTemperature(), config.getMaxTokens(), 
                config.isStream());
        
        try {
            // 1. 从输入中获取必要的参数
            String prompt = buildPrompt(input);
            String platform = getPlatform(input);
            String modelName = getModelName(input);
            ChatType chatType = getChatType(input);
            
            // 2. 构建请求对象
            Lc4jRequest request = Lc4jRequest.builder()
                    .platform(platform)
                    .model(modelName)
                    .prompt(prompt)
                    .chatType(chatType)
                    .build();
            
            // 3. 根据 chatType 选择同步或流式调用
            if (chatType == ChatType.STREAM) {
                return executeStream(request);
            } else {
                return executeSync(request);
            }
            
        } catch (Exception e) {
            log.error("LLM 调用节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("LLM 调用节点执行失败：" + e.getMessage());
            return output;
        }
    }
    
    /**
     * 同步执行 LLM 调用
     */
    private NodeOutput executeSync(Lc4jRequest request) {
        Lc4jResponse response = lc4jService.call(request);
        
        NodeOutput output = new NodeOutput();
        output.setSuccess(response.isSuccess());
        output.setMsg(response.getErrorMessage() != null ? response.getErrorMessage() : "LLM 调用成功");
        
        if (response.isSuccess()) {
            output.addData("content", response.getContent());
            output.addData("platform", response.getPlatform());
            output.addData("model", response.getModel());
            output.addData("messages", response.getContent());
            
            log.info("LLM 调用成功，平台：{}, 模型：{}", response.getPlatform(), response.getModel());
        } else {
            log.error("LLM 调用失败：{}", response.getErrorMessage());
        }
        
        log.info("LLM 同步调用节点执行完成");
        return output;
    }
    
    /**
     * 流式执行 LLM 调用（收集所有块后返回）
     * 注意：由于节点接口是同步的，这里会收集所有流式块后一次性返回
     * 真正的流式需要在 Graph 执行层面处理
     */
    private NodeOutput executeStream(Lc4jRequest request) {
        StreamingChatModel streamingChatModel = lc4jService.getStreamingChatModel(request.getPlatform(), request.getModel());
        
        StreamingChatGenerator<AgentState> generator = StreamingChatGenerator.builder()
                .mapResult(r -> {
                    String content = r != null && r.aiMessage() != null ? r.aiMessage().text() : "";
                    return Map.<String, Object>of("content", content);
                })
                .build();

        UserMessage userMessage = UserMessage.from(request.getPrompt());
        streamingChatModel.chat(List.of(userMessage), generator.handler());

        NodeOutput output = new NodeOutput();
        output.setSuccess(true);
        output.setMsg("LLM 流式调用成功");
        output.addData("platform", request.getPlatform());
        output.addData("model", request.getModel());
        output.addData("streamingChatGenerator", generator);
        output.addData("stream", true);
        output.addData("chatType", ChatType.STREAM.name());
        
        log.info("LLM 流式调用完成");
        return output;
    }
    
    /**
     * 构建 Prompt
     * @param input 节点输入
     * @return 构建后的 Prompt
     */
    private String buildPrompt(NodeInput input) {
        // 优先使用配置的模板
        if (config.getPromptTemplate() != null && !config.getPromptTemplate().isEmpty()) {
            return applyPromptTemplate(config.getPromptTemplate(), input);
        }
        
        // 否则从输入中获取
        String prompt = input.getParameter("prompt", "");
        if (prompt.trim().isEmpty()) {
            prompt = input.getParameter("query", "");
        }
        if (prompt.trim().isEmpty()) {
            prompt = input.getParameter("userInput", "");
        }
        
        // 如果还是为空，使用默认值
        if (prompt.trim().isEmpty()) {
            prompt = config.getDefaultPrompt();
        }
        
        return prompt != null ? prompt : "";
    }
    
    /**
     * 应用 Prompt 模板
     * @param template 模板字符串
     * @param input 输入参数
     * @return 替换后的 Prompt
     */
    private String applyPromptTemplate(String template, NodeInput input) {
        String result = template;
        
        // 简单的变量替换（支持 {variable} 格式）
        for (Map.Entry<String, Object> entry : input.toMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                result = result.replace("{" + key + "}", value.toString());
            }
        }
        
        return result;
    }
    
    /**
     * 获取 Chat 类型
     * @param input 节点输入
     * @return Chat 类型
     */
    private ChatType getChatType(NodeInput input) {
        // 优先使用输入中的 chatType
        Object chatTypeObj = input.toMap().get("chatType");
        if (chatTypeObj != null) {
            if (chatTypeObj instanceof ChatType) {
                return (ChatType) chatTypeObj;
            } else if (chatTypeObj instanceof String) {
                try {
                    return ChatType.valueOf(((String) chatTypeObj).toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("无效的 ChatType: {}, 使用默认值 SYNC", chatTypeObj);
                }
            }
        }
        
        // 使用节点配置的 stream 属性作为默认值
        return config.isStream() ? ChatType.STREAM : ChatType.SYNC;
    }
    
    /**
     * 获取平台类型
     * @param input 节点输入
     * @return 平台类型
     */
    private String getPlatform(NodeInput input) {
        // 优先使用输入中的平台配置
        String platform = input.getParameter("platform", "OLLAMA");
        if (!platform.trim().isEmpty()) {
            return platform;
        }
        
        // 使用节点配置的平台
        if (config.getPlatform() != null && !config.getPlatform().trim().isEmpty()) {
            return config.getPlatform();
        }
        
        // 使用默认平台
        return "SILICON_FLOW"; // 默认使用 SILICON_FLOW
    }
    
    /**
     * 获取模型名称
     * @param input 节点输入
     * @return 模型名称
     */
    private String getModelName(NodeInput input) {
        // 优先使用输入中的模型配置
        String model = input.getParameter("model", "default");
        if (!model.trim().isEmpty()) {
            return model;
        }
        
        // 使用节点配置的模型
        if (config.getModelName() != null && !config.getModelName().trim().isEmpty()) {
            return config.getModelName();
        }
        
        // 返回 null，使用默认模型
        return null;
    }
}
