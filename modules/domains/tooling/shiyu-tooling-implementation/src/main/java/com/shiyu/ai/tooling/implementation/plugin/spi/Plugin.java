package com.shiyu.ai.tooling.implementation.plugin.spi;

import java.util.Map;

/**
 * 定义 插件 相关的协作契约和调用边界。
 */
public interface Plugin {

    /** 插件唯一标识 */
    String getId();

    /** 插件名称 */
    String getName();

    /** 插件版本 */
    String getVersion();

    /** 插件初始化 */
    default void init(PluginContext context) {}

    /** 插件启动 */
    default void start() {}

    /** 插件停止 */
    default void stop() {}

    /** 插件销毁 */
    default void destroy() {}

    /** 处理自定义请求 */
    default Object execute(String action, Map<String, Object> params) {
        return Map.of("status", "not_implemented", "action", action);
    }
}
