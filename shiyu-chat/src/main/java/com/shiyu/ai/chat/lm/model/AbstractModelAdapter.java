package com.shiyu.ai.chat.lm.model;

import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.chat.lm.request.ModelRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 抽象模型适配器基类
 * 提供通用的实现和错误处理逻辑，不包含客户端相关代码
 */
@Slf4j
public abstract class AbstractModelAdapter implements ModelAdapter {

    /**
     * 执行同步调用（由子类提供具体实现）
     */
    protected abstract String doCall(ModelRequest request);

    /**
     * 执行流式调用（由子类提供具体实现）
     */
    protected abstract Flux<String> doStream(ModelRequest request);

    @Override
    public String call(ModelRequest request) {
        try {
            log.debug("Calling model: {} with prompt: {}", getType(), request.getPrompt());
            String response = doCall(request);
            log.debug("Model: {} responded successfully", getType());
            return response;
            
        } catch (Exception e) {
            log.error("Error calling model: {}. Error: {}", getType(), e.getMessage(), e);
            throw new RuntimeException("Failed to call model: " + getType().name(), e);
        }
    }

    @Override
    public Flux<String> stream(ModelRequest request) {
        try {
            log.debug("Streaming model: {} with prompt: {}", getType(), request.getPrompt());
            Flux<String> response = doStream(request);
            log.debug("Model: {} streaming started", getType());
            return response;
            
        } catch (Exception e) {
            log.error("Error streaming model: {}. Error: {}", getType(), e.getMessage(), e);
            return Flux.error(new RuntimeException("Failed to stream model: " + getType().name(), e));
        }
    }
}
