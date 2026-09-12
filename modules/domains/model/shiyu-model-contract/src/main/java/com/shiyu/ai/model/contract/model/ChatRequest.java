package com.shiyu.ai.model.contract.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ChatRequest 数据对象，承载模型领域相关业务数据。
 */
@Data
@Builder
@AllArgsConstructor
public class ChatRequest {
    /**
     * platform 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String platform;
    /**
     * model 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String model;
    /**
     * modelRouteId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String modelRouteId;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private long tenantId;
    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private long userId;
    /**
     * generationRunId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String generationRunId;
    /**
     * messages 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<ChatMessage> messages;
    /**
     * chatType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private ChatType chatType;
    /**
     * temperature 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double temperature;
    /**
     * maxOutputTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer maxOutputTokens;
    /**
     * reasoningEffort 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String reasoningEffort;
    /**
     * tools 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<ToolDefinition> tools;

    /**
     * {@code ChatRequest} 创建并初始化当前类型实例。
     */
    public ChatRequest() {
        this.messages = List.of();
        this.tools = List.of();
    }

    /**
     * 设置messages。
     *
     * @param messages messages 参数。
     */
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages == null ? List.of() : List.copyOf(messages);
    }

    /**
     * 设置tools。
     *
     * @param tools tools 参数。
     */
    public void setTools(List<ToolDefinition> tools) {
        this.tools = tools == null ? List.of() : List.copyOf(tools);
    }

    /**
     * 描述模型可调用工具的名称、说明和参数 schema。
     * @param name 名称，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param parametersJson parametersJson 属性，表示该记录组件承载的数据。
     */
    public record ToolDefinition(String name, String description, String parametersJson) {}
}
