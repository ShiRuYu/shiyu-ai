package com.shiyu.ai.tooling.implementation.tool.mcp;

import java.util.List;
import java.util.Map;

/** MCP 工具描述符 描述一个可注册和发现的工具 */
public class McpToolDescriptor {

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private final String description;
    /**
     * 服务标识，表示当前对象中的对应属性。
     */
    private final String serverId;
    /**
     * parameters 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Map<String, ParameterInfo> parameters;
    /**
     * tags 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<String> tags;
    /**
     * category 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String category;
    /**
     * builtin 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final boolean builtin;
    /**
     * registeredAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long registeredAt;

    /**
     * {@code McpToolDescriptor} 创建并初始化当前类型实例。
     *
     * @param name 参数值，用于执行当前操作。
     * @param description 参数值，用于执行当前操作。
     * @param serverId 参数值，用于执行当前操作。
     * @param parameters 参数值，用于执行当前操作。
     */
    public McpToolDescriptor(
            String name,
            String description,
            String serverId,
            Map<String, ParameterInfo> parameters) {
        this(name, description, serverId, parameters, List.of(), "default", false);
    }

    /**
     * {@code McpToolDescriptor} 创建并初始化当前类型实例。
     *
     * @param name 参数值，用于执行当前操作。
     * @param description 参数值，用于执行当前操作。
     * @param serverId 参数值，用于执行当前操作。
     * @param parameters 参数值，用于执行当前操作。
     * @param tags 参数值，用于执行当前操作。
     * @param category 参数值，用于执行当前操作。
     * @param builtin 参数值，用于执行当前操作。
     */
    public McpToolDescriptor(
            String name,
            String description,
            String serverId,
            Map<String, ParameterInfo> parameters,
            List<String> tags,
            String category,
            boolean builtin) {
        this.name = name;
        this.description = description;
        this.serverId = serverId;
        this.parameters = parameters;
        this.tags = tags;
        this.category = category;
        this.builtin = builtin;
        this.registeredAt = System.currentTimeMillis();
    }

    /**
     * {@code getName} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getName() {
        return name;
    }

    /**
     * {@code getDescription} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@code getServerId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * {@code getParameters} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, ParameterInfo> getParameters() {
        return parameters;
    }

    /**
     * {@code getTags} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * {@code getCategory} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getCategory() {
        return category;
    }

    /**
     * {@code isBuiltin} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isBuiltin() {
        return builtin;
    }

    /**
     * {@code getRegisteredAt} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getRegisteredAt() {
        return registeredAt;
    }

    /**
     * 参数信息
     *
     * @param type 类型，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param required 是否必填，表示该记录组件承载的数据。
     * @param defaultValue defaultValue 属性，表示该记录组件承载的数据。
     */
    public record ParameterInfo(
            String type, String description, boolean required, Object defaultValue) {
        /**
         * {@code ParameterInfo} 创建并初始化当前类型实例。
         *
         * @param type 参数值，用于执行当前操作。
         * @param description 参数值，用于执行当前操作。
         * @param required 参数值，用于执行当前操作。
         */
        public ParameterInfo(String type, String description, boolean required) {
            this(type, description, required, null);
        }
    }
}
