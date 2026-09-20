package com.shiyu.ai.model.implementation.infrastructure.port;

import com.shiyu.ai.model.implementation.infrastructure.adapter.config.PlatformConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * 将 模型 在不同层之间进行适配、转换或组装。
 */
public interface ModelAdapter {

    /**
     * 查询 模型 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 相关操作生成的结果数据。
     */
    String getPlatformType();

    /**
     * 查询 模型 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 模型 相关操作生成的结果数据。
     */
    ChatModel getChatModel(String modelName);

    /**
     * 查询 模型 相关业务数据，并返回处理结果。
     *
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 模型 相关操作生成的结果数据。
     */
    StreamingChatModel getStreamingChatModel(String modelName);

    /**
     * 查询 模型 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 相关操作生成的结果数据。
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
     * 创建或保存 模型 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 模型 相关操作生成的结果数据。
     */
    ChatModel createChatModel(PlatformConfig config, String modelName);

    /**
     * 创建或保存 模型 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @param modelName 用于完成本次业务处理的 modelName 参数。
     * @return 返回 模型 相关操作生成的结果数据。
     */
    StreamingChatModel createStreamingChatModel(PlatformConfig config, String modelName);
}
