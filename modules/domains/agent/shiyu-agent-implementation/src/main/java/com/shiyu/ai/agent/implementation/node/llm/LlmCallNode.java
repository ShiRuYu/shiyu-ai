package com.shiyu.ai.agent.implementation.node.llm;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;
import com.shiyu.ai.model.contract.model.ChatType;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.bsc.langgraph4j.langchain4j.generators.StreamingChatGenerator;
import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code LlmCallNode} 承载智能体模块中的智能流程节点，负责执行本节点的输入处理与结果产出。
 */
@Setter
@Getter
@Slf4j
public class LlmCallNode extends BaseNode {

    /**
     * 配置，表示当前对象中的对应属性。
     */
    private LlmCallConfig config;

    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    private LlmCallNode(LlmCallConfig config, ChatEngine chatEngine) {
        super(config != null ? config : new LlmCallConfig());
        this.config = config != null ? config : new LlmCallConfig();
        this.config.setNodeType(NodeType.LLM_CALL);
        this.chatEngine = chatEngine;
    }

    /**
     * {@code builder} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@code Builder} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    public static class Builder {
        /**
         * 配置，表示当前对象中的对应属性。
         */
        private LlmCallConfig config;
        /**
         * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
         */
        private ChatEngine chatEngine;

        /**
         * {@code config} 执行当前类型定义的业务操作。
         *
         * @param config 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder config(LlmCallConfig config) {
            this.config = config;
            return this;
        }

        /**
         * {@code chatEngine} 执行当前类型定义的业务操作。
         *
         * @param chatEngine 参数值，用于执行当前操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public Builder chatEngine(ChatEngine chatEngine) {
            this.chatEngine = chatEngine;
            return this;
        }

        /**
         * {@code build} 执行当前类型定义的业务操作。
         *
         * @return 返回当前操作产生的结果。
         */
        public LlmCallNode build() {
            if (chatEngine == null) {
                throw new IllegalStateException("创建 LlmCallNode 失败：chatEngine 不能为空");
            }
            return new LlmCallNode(config, chatEngine);
        }
    }

    /**
     * {@code doExecute} 执行当前类型定义的业务操作。
     *
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 LLM 调用节点：{}", config.getNodeName());
        log.debug(
                "LLM 配置：modelName={}, temperature={}, maxTokens={}, stream={}",
                config.getModelName(),
                config.getTemperature(),
                config.getMaxTokens(),
                config.isStream());

        try {
            String prompt = buildPrompt(input);
            String platform = getPlatform(input);
            String modelName = getModelName(input, platform);
            ChatType chatType = getChatType(input);

            ChatRequest request =
                    ChatRequest.builder()
                            .platform(platform)
                            .model(modelName)
                            .tenantId(
                                    requiredPositiveLong(
                                            input.getParameter("tenantId", null), "tenantId"))
                            .userId(
                                    requiredPositiveLong(
                                            input.getParameter("userId", null), "userId"))
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
            output.setMsg(isInputValidationFailure(e) ? e.getMessage() : "LLM 调用节点执行失败，请稍后重试");
            return output;
        }
    }

    private boolean isInputValidationFailure(Exception exception) {
        if (!(exception instanceof IllegalStateException) || exception.getMessage() == null)
            return false;
        String message = exception.getMessage();
        return message.endsWith(" is required")
                || message.endsWith(" must be positive")
                || message.endsWith(" must be a positive number");
    }

    private NodeOutput executeSync(ChatRequest request) {
        ChatResponse response = chatEngine.chat(request);

        NodeOutput output = new NodeOutput();
        output.setSuccess(response.isSuccess());
        output.setMsg(response.isSuccess() ? "LLM 调用成功" : "LLM 调用失败，请稍后重试");

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
        return executeStructuredStream(request);
    }

    private long requiredPositiveLong(Object value, String field) {
        long parsed;
        if (value instanceof Number number) {
            parsed = number.longValue();
        } else if (value != null) {
            try {
                parsed = Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException exception) {
                throw new IllegalStateException(field + " must be a positive number", exception);
            }
        } else {
            throw new IllegalStateException(field + " is required");
        }
        if (parsed <= 0) {
            throw new IllegalStateException(field + " must be positive");
        }
        return parsed;
    }

    private NodeOutput executeStructuredStream(ChatRequest request) {
        StreamingChatGenerator<AgentState> generator =
                StreamingChatGenerator.builder()
                        .mapResult(
                                r -> {
                                    String content =
                                            r != null && r.aiMessage() != null
                                                    ? r.aiMessage().text()
                                                    : "";
                                    return Map.<String, Object>of(FieldKey.CONTENT.key(), content);
                                })
                        .build();
        StringBuilder answer = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        chatEngine.stream(request)
                .subscribe(
                        event -> {
                            if ("DELTA".equals(event.getEventType())
                                    && event.getContent() != null) {
                                answer.append(event.getContent());
                                generator.handler().onPartialResponse(event.getContent());
                            } else if ("REASONING_DELTA".equals(event.getEventType())
                                    && event.getReasoningContent() != null) {
                                reasoning.append(event.getReasoningContent());
                            }
                        },
                        generator.handler()::onError,
                        () ->
                                generator
                                        .handler()
                                        .onCompleteResponse(
                                                dev.langchain4j.model.chat.response.ChatResponse
                                                        .builder()
                                                        .aiMessage(
                                                                dev.langchain4j.data.message
                                                                        .AiMessage.builder()
                                                                        .text(answer.toString())
                                                                        .thinking(
                                                                                reasoning
                                                                                        .toString())
                                                                        .build())
                                                        .modelName(request.getModel())
                                                        .build()));
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
    private List<com.shiyu.ai.model.contract.model.ChatMessage> buildMessages(
            NodeInput input, String prompt) {
        Object raw = input.toMap().get(FieldKey.MESSAGES.key());
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of(com.shiyu.ai.model.contract.model.ChatMessage.text("user", prompt));
        }
        List<com.shiyu.ai.model.contract.model.ChatMessage> messages = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof com.shiyu.ai.model.contract.model.ChatMessage message) {
                messages.add(message);
            } else if (item instanceof Map<?, ?> map) {
                String role = Objects.toString(map.get("role"), "user");
                Object content = map.get("content");
                messages.add(
                        com.shiyu.ai.model.contract.model.ChatMessage.text(
                                role, Objects.toString(content, "")));
            }
        }
        return messages.isEmpty()
                ? List.of(com.shiyu.ai.model.contract.model.ChatMessage.text("user", prompt))
                : List.copyOf(messages);
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

        return null;
    }

    /**
     * {@code getRequiredInputs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.apiOptional("query", "string", "用户提问/输入文本", ""),
                NodeInputParam.config("platform", "string", "AI 平台编码（如 SILICON_FLOW）"),
                NodeInputParam.config("modelName", "string", "模型名称"),
                NodeInputParam.config("temperature", "number", "温度参数"),
                NodeInputParam.defaultVal(
                        "defaultPrompt", "string", "默认 Prompt（无 query 时使用）", "你是一个智能助手"));
    }
}
