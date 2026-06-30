package com.shiyu.ai.core.langchain4j;

import com.shiyu.ai.core.langchain4j.config.PlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

public interface ModelAdapter {

    String getPlatformType();

    ChatModel getChatModel(String modelName);

    StreamingChatModel getStreamingChatModel(String modelName);

    String getDefaultModelName();

    boolean isAvailable();

    void clearCache();

    ChatModel createChatModel(PlatformConfig config, String modelName);

    StreamingChatModel createStreamingChatModel(PlatformConfig config, String modelName);
}
