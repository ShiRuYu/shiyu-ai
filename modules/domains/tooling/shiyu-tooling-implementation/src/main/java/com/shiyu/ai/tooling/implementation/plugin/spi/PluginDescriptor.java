package com.shiyu.ai.tooling.implementation.plugin.spi;

import java.util.Map;

/**
 * 实现 插件 Descriptor 相关的业务处理、协作逻辑或基础设施能力。
 */
public class PluginDescriptor {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private final String id;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
    /**
     * 版本，表示当前对象中的对应属性。
     */
    private final String version;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private final String description;
    /**
     * author 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String author;
    /**
     * entryClass 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String entryClass;
    /**
     * 配置，表示当前对象中的对应属性。
     */
    private final Map<String, Object> config;
    /**
     * loadedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long loadedAt;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private volatile PluginState state;

    /**
     * 定义 插件 可用的枚举值及其业务语义。
     */
    public enum PluginState {
        INSTALLED,
        RESOLVED,
        STARTING,
        ACTIVE,
        STOPPING,
        STOPPED,
        FAILED
    }

    /**
     * 执行 插件 Descriptor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param description 用于完成本次业务处理的 description 参数。
     * @param author 用于完成本次业务处理的 author 参数。
     * @param entryClass 用于完成本次业务处理的 entryClass 参数。
     * @param config 用于完成本次业务处理的 config 参数。
     */
    public PluginDescriptor(
            String id,
            String name,
            String version,
            String description,
            String author,
            String entryClass,
            Map<String, Object> config) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;
        this.entryClass = entryClass;
        this.config = config;
        this.loadedAt = System.currentTimeMillis();
        this.state = PluginState.INSTALLED;
    }

    // Getters
    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public String getId() {
        return id;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public String getName() {
        return name;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public String getVersion() {
        return version;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public String getDescription() {
        return description;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public String getEntryClass() {
        return entryClass;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public Map<String, Object> getConfig() {
        return config;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public long getLoadedAt() {
        return loadedAt;
    }

    /**
     * 查询 插件 Descriptor 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Descriptor 相关操作生成的结果数据。
     */
    public PluginState getState() {
        return state;
    }

    /**
     * 更新或设置 插件 Descriptor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param state 用于完成本次业务处理的 state 参数。
     */
    public void setState(PluginState state) {
        this.state = state;
    }
}
