package com.shiyu.ai.tooling.implementation.tool.mcp;

import java.util.List;
import java.util.Map;

/**
 * 实现 Mcp 工具 Descriptor 相关的业务处理、协作逻辑或基础设施能力。
 */
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
     * 执行 Mcp 工具 Descriptor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param description 用于完成本次业务处理的 description 参数。
     * @param serverId 用于定位server的标识。
     * @param parameters 用于完成本次业务处理的 parameters 参数。
     */
    public McpToolDescriptor(
            String name,
            String description,
            String serverId,
            Map<String, ParameterInfo> parameters) {
        this(name, description, serverId, parameters, List.of(), "default", false);
    }

    /**
     * 执行 Mcp 工具 Descriptor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param description 用于完成本次业务处理的 description 参数。
     * @param serverId 用于定位server的标识。
     * @param parameters 用于完成本次业务处理的 parameters 参数。
     * @param tags 用于完成本次业务处理的 tags 参数。
     * @param category 用于完成本次业务处理的 category 参数。
     * @param builtin 用于完成本次业务处理的 builtin 参数。
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
     * 查询 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mcp 工具 Descriptor 相关操作生成的结果数据。
     */
    public String getName() {
        return name;
    }

    /**
     * 查询 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mcp 工具 Descriptor 相关操作生成的结果数据。
     */
    public String getDescription() {
        return description;
    }

    /**
     * 查询 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mcp 工具 Descriptor 相关操作生成的结果数据。
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * 查询 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mcp 工具 Descriptor 相关操作生成的结果数据。
     */
    public Map<String, ParameterInfo> getParameters() {
        return parameters;
    }

    /**
     * 查询 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * 查询 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mcp 工具 Descriptor 相关操作生成的结果数据。
     */
    public String getCategory() {
        return category;
    }

    /**
     * 校验或判断 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public boolean isBuiltin() {
        return builtin;
    }

    /**
     * 查询 Mcp 工具 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mcp 工具 Descriptor 相关操作生成的结果数据。
     */
    public long getRegisteredAt() {
        return registeredAt;
    }

    /**
     * 封装 Parameter Info 相关的不可变数据及其字段约束。
     */
    public record ParameterInfo(
            String type, String description, boolean required, Object defaultValue) {
        /**
         * 执行 Parameter Info 相关业务操作，并维护必要的状态和协作关系。
         *
         * @param type 用于完成本次业务处理的 type 参数。
         * @param description 用于完成本次业务处理的 description 参数。
         * @param required 用于完成本次业务处理的 required 参数。
         */
        public ParameterInfo(String type, String description, boolean required) {
            this(type, description, required, null);
        }
    }
}
