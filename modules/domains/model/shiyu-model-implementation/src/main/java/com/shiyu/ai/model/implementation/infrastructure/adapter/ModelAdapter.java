package com.shiyu.ai.model.implementation.infrastructure.adapter;

import com.shiyu.ai.model.implementation.infrastructure.adapter.config.PlatformConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * ModelAdapter 接口，定义模型模块的能力边界。
 */
public interface ModelAdapter {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 操作结果。
     */
    String getPlatformType();

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param modelName 方法参数。
     *
     * @return 操作结果。
     */
    ChatModel getChatModel(String modelName);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param modelName 方法参数。
     *
     * @return 操作结果。
     */
    StreamingChatModel getStreamingChatModel(String modelName);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 操作结果。
     */
    String getDefaultModelName();

    /**
     * 判断当前条件是否满足。
     *
     * @return 条件是否满足。
     */
    boolean isAvailable();

    /**
     * 删除指定业务对象或关联数据。
     */
    void clearCache();

    /**
     * 创建并保存业务对象。
     *
     * @param config 配置参数。
     * @param modelName 方法参数。
     *
     * @return 操作结果。
     */
    ChatModel createChatModel(PlatformConfig config, String modelName);

    /**
     * 创建并保存业务对象。
     *
     * @param config 配置参数。
     * @param modelName 方法参数。
     *
     * @return 操作结果。
     */
    StreamingChatModel createStreamingChatModel(PlatformConfig config, String modelName);
}
