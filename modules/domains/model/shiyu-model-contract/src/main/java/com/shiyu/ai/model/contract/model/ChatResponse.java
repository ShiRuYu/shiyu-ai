package com.shiyu.ai.model.contract.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ChatResponse 数据对象，承载模型领域相关业务数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    /**
     * success 属性，保存当前对象中的业务数据或协作依赖。
     */
    private boolean success;
    /**
     * 内容，表示当前对象中的对应属性。
     */
    private String content;
    /**
     * reasoningContent 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String reasoningContent;
    /**
     * platform 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String platform;
    /**
     * model 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String model;
    /**
     * 错误消息，表示当前对象中的对应属性。
     */
    private String errorMessage;
    /**
     * eventType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String eventType;
    /**
     * promptTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer promptTokens;
    /**
     * completionTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer completionTokens;
    /**
     * totalTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer totalTokens;
    /**
     * 预计用量，表示当前对象中的对应属性。
     */
    private boolean estimatedUsage;
    /**
     * toolCallId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String toolCallId;
    /**
     * toolName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String toolName;
    /**
     * toolArguments 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String toolArguments;
    /**
     * blockIndex 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer blockIndex;
    /**
     * finishReason 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String finishReason;
    /**
     * 提供者请求标识，表示当前对象中的对应属性。
     */
    private String providerRequestId;
    /**
     * cacheReadTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer cacheReadTokens;
    /**
     * cacheWriteTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer cacheWriteTokens;
    /**
     * reasoningTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer reasoningTokens;
    /**
     * toolCalls 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<ToolCall> toolCalls;

    /**
     * 表示模型生成的一次工具调用及其参数。
     * @param id 标识，表示该记录组件承载的数据。
     * @param name 名称，表示该记录组件承载的数据。
     * @param arguments arguments 属性，表示该记录组件承载的数据。
     */
    public record ToolCall(String id, String name, String arguments) {}
}
