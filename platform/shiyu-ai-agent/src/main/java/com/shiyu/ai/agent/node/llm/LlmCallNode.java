package com.shiyu.ai.agent.node.llm;

import com.shiyu.ai.model.ChatType;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.NodeFields.FieldKey;
import com.shiyu.ai.model.adapter.ModelManager;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.langchain4j.generators.StreamingChatGenerator;
import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;
import com.shiyu.ai.agent.node.NodeInputParam;

@Setter
@Getter
@Slf4j
public class LlmCallNode extends BaseNode {

    private LlmCallConfig config;

    private final ChatEngine chatEngine;
    private final ModelManager modelManager;

    private LlmCallNode(LlmCallConfig config, ChatEngine chatEngine, ModelManager modelManager) {
        super(config != null ? config : new LlmCallConfig());
        this.config = config != null ? config : new LlmCallConfig();
        this.config.setNodeType(NodeType.LLM_CALL);
        this.chatEngine = chatEngine;
        this.modelManager = modelManager;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LlmCallConfig config;
        private ChatEngine chatEngine;
        private ModelManager modelManager;

        public Builder config(LlmCallConfig config) {
            this.config = config;
            return this;
        }

        public Builder chatEngine(ChatEngine chatEngine) {
            this.chatEngine = chatEngine;
            return this;
        }

        public Builder modelManager(ModelManager modelManager) {
            this.modelManager = modelManager;
            return this;
        }

        public LlmCallNode build() {
            if (chatEngine == null) {
                throw new IllegalStateException("创建 LlmCallNode 失败：chatEngine 不能为空");
            }
            if (modelManager == null) {
                throw new IllegalStateException("创建 LlmCallNode 失败：modelManager 不能为空");
            }
            return new LlmCallNode(config, chatEngine, modelManager);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 LLM 调用节点：{}", config.getNodeName());
        log.debug("LLM 配置：modelName={}, temperature={}, maxTokens={}, stream={}",
                config.getModelName(), config.getTemperature(), config.getMaxTokens(),
                config.isStream());

        try {
            String prompt = buildPrompt(input);
            String platform = getPlatform(input);
            String modelName = getModelName(input, platform);
            ChatType chatType = getChatType(input);

            ChatRequest request = ChatRequest.builder()
                    .platform(platform)
                    .model(modelName)
                    .messages(buildMessages(input, prompt))
                    .chatType(chatType)
                    .build();

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

    private NodeOutput executeSync(ChatRequest request) {
        ChatResponse response = chatEngine.chat(request);

        NodeOutput output = new NodeOutput();
        output.setSuccess(response.isSuccess());
        output.setMsg(response.getErrorMessage() != null ? response.getErrorMessage() : "LLM 调用成功");

        if (response.isSuccess()) {
            output.addData(FieldKey.CONTENT, response.getContent());
            output.addData(FieldKey.PLATFORM_OUTPUT, response.getPlatform());
            output.addData(FieldKey.MODEL_OUTPUT, response.getModel());

            log.info("LLM 调用成功，平台：{}, 模型：{}", response.getPlatform(), response.getModel());
        } else {
            log.error("LLM 调用失败：{}", response.getErrorMessage());
        }

        log.info("LLM 同步调用节点执行完成");
        return output;
    }

    private NodeOutput executeStream(ChatRequest request) {
        // DeepSeek (and future structured providers) must use the same
        // provider-neutral gateway as synchronous calls.  Do not convert the
        // request back into a single LangChain user prompt, otherwise
        // reasoning/tool/content-part events are lost before they reach the
        // Runtime event log.
        if ("DEEPSEEK".equalsIgnoreCase(request.getPlatform())) {
            return executeStructuredStream(request);
        }
        StreamingChatModel streamingChatModel = modelManager.getStreamingChatModel(request.getPlatform(), request.getModel());

        StreamingChatGenerator<AgentState> generator = StreamingChatGenerator.builder()
                .mapResult(r -> {
                    String content = r != null && r.aiMessage() != null ? r.aiMessage().text() : "";
                    return Map.<String, Object>of(FieldKey.CONTENT.key(), content);
                })
                .build();

        List<dev.langchain4j.data.message.ChatMessage> userMessages = request.getMessages().stream()
                .map(message -> (dev.langchain4j.data.message.ChatMessage) UserMessage.from(message.content().stream().map(com.shiyu.ai.model.chat.ChatMessage.ContentPart::text)
                        .filter(java.util.Objects::nonNull).reduce("", String::concat)))
                .toList();
        streamingChatModel.chat(userMessages, generator.handler());

        NodeOutput output = new NodeOutput();
        output.setSuccess(true);
        output.setMsg("LLM 流式调用成功");
        output.addData(FieldKey.PLATFORM_OUTPUT, request.getPlatform());
        output.addData(FieldKey.MODEL_OUTPUT, request.getModel());
        output.addData(FieldKey.STREAM, true);
        output.addData(FieldKey.CHAT_TYPE, ChatType.STREAM.name());
        output.addData(FieldKey.STREAMING_GENERATOR, generator);

        log.info("LLM 流式调用完成");
        return output;
    }

    private NodeOutput executeStructuredStream(ChatRequest request) {
        StreamingChatGenerator<AgentState> generator = StreamingChatGenerator.builder()
                .mapResult(r -> {
                    String content = r != null && r.aiMessage() != null ? r.aiMessage().text() : "";
                    return Map.<String, Object>of(FieldKey.CONTENT.key(), content);
                })
                .build();
        StringBuilder answer = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        chatEngine.stream(request).subscribe(event -> {
            if ("DELTA".equals(event.getEventType()) && event.getContent() != null) {
                answer.append(event.getContent());
                generator.handler().onPartialResponse(event.getContent());
            } else if ("REASONING_DELTA".equals(event.getEventType()) && event.getReasoningContent() != null) {
                reasoning.append(event.getReasoningContent());
            }
        }, generator.handler()::onError, () -> generator.handler().onCompleteResponse(
                dev.langchain4j.model.chat.response.ChatResponse.builder()
                        .aiMessage(dev.langchain4j.data.message.AiMessage.builder()
                                .text(answer.toString()).thinking(reasoning.toString()).build())
                        .modelName(request.getModel()).build()));
        NodeOutput output = new NodeOutput();
        output.setSuccess(true);
        output.setMsg("LLM 结构化流式调用成功");
        output.addData(FieldKey.PLATFORM_OUTPUT, request.getPlatform());
        output.addData(FieldKey.MODEL_OUTPUT, request.getModel());
        output.addData(FieldKey.STREAM, true);
        output.addData(FieldKey.CHAT_TYPE, ChatType.STREAM.name());
        output.addData(FieldKey.STREAMING_GENERATOR, generator);
        return output;
    }

    @SuppressWarnings("unchecked")
    private List<com.shiyu.ai.model.chat.ChatMessage> buildMessages(NodeInput input, String prompt) {
        Object raw = input.toMap().get(FieldKey.MESSAGES.key());
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of(com.shiyu.ai.model.chat.ChatMessage.text("user", prompt));
        }
        List<com.shiyu.ai.model.chat.ChatMessage> messages = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof com.shiyu.ai.model.chat.ChatMessage message) {
                messages.add(message);
            } else if (item instanceof Map<?, ?> map) {
                String role = Objects.toString(map.get("role"), "user");
                Object content = map.get("content");
                messages.add(com.shiyu.ai.model.chat.ChatMessage.text(role, Objects.toString(content, "")));
            }
        }
        return messages.isEmpty() ? List.of(com.shiyu.ai.model.chat.ChatMessage.text("user", prompt)) : List.copyOf(messages);
    }

    private String buildPrompt(NodeInput input) {
        if (config.getPromptTemplate() != null && !config.getPromptTemplate().isEmpty()) {
            return applyPromptTemplate(config.getPromptTemplate(), input);
        }

        String prompt = input.getParameter(FieldKey.QUERY, null);
        if (prompt != null && !prompt.trim().isEmpty()) {
            return prompt;
        }

        if (config.getDefaultPrompt() != null && !config.getDefaultPrompt().isEmpty()) {
            return config.getDefaultPrompt();
        }

        return "你是一个智能助手，请友好地回答用户的问题。";
    }

    private String applyPromptTemplate(String template, NodeInput input) {
        String result = template;

        for (Map.Entry<String, Object> entry : input.toMap().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                result = result.replace("{" + key + "}", value.toString());
            }
        }

        return result;
    }

    private ChatType getChatType(NodeInput input) {
        Object chatTypeObj = input.toMap().get(FieldKey.CHAT_TYPE.key());
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

        return config.isStream() ? ChatType.STREAM : ChatType.SYNC;
    }

    private String getPlatform(NodeInput input) {
        String platform = input.getParameter(FieldKey.PLATFORM, null);
        if (platform != null && !platform.trim().isEmpty()) {
            return platform;
        }

        if (config.getPlatform() != null && !config.getPlatform().trim().isEmpty()) {
            return config.getPlatform();
        }

        try {
            String defaultPlatform = modelManager.getDefaultPlatform();
            if (defaultPlatform != null && !defaultPlatform.trim().isEmpty()) {
                return defaultPlatform;
            }
        } catch (Exception e) {
            log.warn("获取默认平台失败: {}", e.getMessage());
        }

        return "SILICON_FLOW";
    }

    private String getModelName(NodeInput input, String platform) {
        String model = input.getParameter(FieldKey.MODEL, null);
        if (model != null && !model.trim().isEmpty()) {
            return model;
        }

        if (config.getModelName() != null && !config.getModelName().trim().isEmpty()) {
            return config.getModelName();
        }

        try {
            String defaultModel = modelManager.getDefaultModelName(platform);
            if (defaultModel != null && !defaultModel.trim().isEmpty()) {
                return defaultModel;
            }
        } catch (Exception e) {
            log.warn("获取默认模型失败: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.apiOptional("query", "string", "用户提问/输入文本", ""),
            NodeInputParam.config("platform", "string", "AI 平台编码（如 SILICON_FLOW）"),
            NodeInputParam.config("modelName", "string", "模型名称"),
            NodeInputParam.config("temperature", "number", "温度参数"),
            NodeInputParam.defaultVal("defaultPrompt", "string", "默认 Prompt（无 query 时使用）", "你是一个智能助手")
        );
    }
}
