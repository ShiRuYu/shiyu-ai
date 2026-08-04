package com.shiyu.ai.tool.mcp;

import java.util.List;
import java.util.Map;

/**
 * MCP 工具描述符
 * 描述一个可注册和发现的工具
 */
public class McpToolDescriptor {

    private final String name;
    private final String description;
    private final String serverId;
    private final Map<String, ParameterInfo> parameters;
    private final List<String> tags;
    private final String category;
    private final boolean builtin;
    private final long registeredAt;

    public McpToolDescriptor(String name, String description, String serverId,
                             Map<String, ParameterInfo> parameters) {
        this(name, description, serverId, parameters, List.of(), "default", false);
    }

    public McpToolDescriptor(String name, String description, String serverId,
                             Map<String, ParameterInfo> parameters,
                             List<String> tags, String category, boolean builtin) {
        this.name = name;
        this.description = description;
        this.serverId = serverId;
        this.parameters = parameters;
        this.tags = tags;
        this.category = category;
        this.builtin = builtin;
        this.registeredAt = System.currentTimeMillis();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getServerId() { return serverId; }
    public Map<String, ParameterInfo> getParameters() { return parameters; }
    public List<String> getTags() { return tags; }
    public String getCategory() { return category; }
    public boolean isBuiltin() { return builtin; }
    public long getRegisteredAt() { return registeredAt; }

    /**
     * 参数信息
     */
    public record ParameterInfo(
            String type,
            String description,
            boolean required,
            Object defaultValue
    ) {
        public ParameterInfo(String type, String description, boolean required) {
            this(type, description, required, null);
        }
    }
}
