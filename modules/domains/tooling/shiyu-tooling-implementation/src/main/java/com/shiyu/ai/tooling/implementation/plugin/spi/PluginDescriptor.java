package com.shiyu.ai.tooling.implementation.plugin.spi;

import java.util.Map;

/** 插件描述符 */
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
     * {@code PluginState} 表示工具模块中的一组受控业务状态或分类。
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
     * {@code PluginDescriptor} 创建并初始化当前类型实例。
     *
     * @param id 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     * @param description 参数值，用于执行当前操作。
     * @param author 参数值，用于执行当前操作。
     * @param entryClass 参数值，用于执行当前操作。
     * @param config 参数值，用于执行当前操作。
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
     * {@code getId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getId() {
        return id;
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
     * {@code getVersion} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getVersion() {
        return version;
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
     * {@code getAuthor} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getAuthor() {
        return author;
    }

    /**
     * {@code getEntryClass} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getEntryClass() {
        return entryClass;
    }

    /**
     * {@code getConfig} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> getConfig() {
        return config;
    }

    /**
     * {@code getLoadedAt} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getLoadedAt() {
        return loadedAt;
    }

    /**
     * {@code getState} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public PluginState getState() {
        return state;
    }

    /**
     * {@code setState} 写入或更新当前模块中的业务数据。
     *
     * @param state 参数值，用于执行当前操作。
     */
    public void setState(PluginState state) {
        this.state = state;
    }
}
