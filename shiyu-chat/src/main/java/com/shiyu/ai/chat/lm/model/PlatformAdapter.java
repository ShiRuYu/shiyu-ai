package com.shiyu.ai.chat.lm.model;

import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import reactor.core.publisher.Flux;

/**
 * 平台适配器接口
 * 用于适配不同的平台提供商
 */
public interface PlatformAdapter {
    
    /**
     * 获取平台类型枚举
     * @return 平台类型
     */
    PlatformEnum getType();
    
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
