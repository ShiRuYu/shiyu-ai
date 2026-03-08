package com.shiyu.ai.chat.lm.model;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

/**
 * 大模型适配器接口
 * 用于适配不同的大语言模型提供商
 */
public interface ModelAdapter {
    
    /**
     * 获取模型类型枚举
     * @return 模型类型
     */
    ModelEnum getType();
    
    /**
     * 获取 ChatClient 实例
     * @return ChatClient
     */
    ChatClient getChatClient();
    
    /**
     * 同步调用模型
     * @param request 请求参数
     * @return 模型响应结果
     */
    String call(ModelRequest request);
    
    /**
     * 流式调用模型
     * @param request 请求参数
     * @return 流式响应
     */
    Flux<String> stream(ModelRequest request);
}
