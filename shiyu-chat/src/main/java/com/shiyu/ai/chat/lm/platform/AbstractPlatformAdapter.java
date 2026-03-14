package com.shiyu.ai.chat.lm.platform;

import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.lm.result.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * 抽象平台适配器基类
 * 提供通用的实现和错误处理逻辑，不包含客户端相关代码
 */
@Slf4j
public abstract class AbstractPlatformAdapter implements PlatformAdapter {

    protected RestClient.Builder restClientBuilder;
    protected WebClient.Builder webClientBuilder;

    /**
     * 执行同步调用（由子类提供具体实现）
     */
    protected abstract ChatResult doCall(LmRequest request);

    /**
     * 执行流式调用（由子类提供具体实现）
     */
    protected abstract StreamResult doStream(LmRequest request);

    @Override
    public ChatResult call(LmRequest request) {
        try {
            log.debug("Calling model: {} with prompt: {}", getType(), request.getPrompt());
            ChatResult response = doCall(request);
            log.debug("Model: {} responded successfully", getType());
            return response;
            
        } catch (Exception e) {
            log.error("Error calling model: {}. Error: {}", getType(), e.getMessage(), e);
            throw new RuntimeException("Failed to call model: " + getType().name(), e);
        }
    }

    @Override
    public StreamResult stream(LmRequest request) {
        try {
            log.debug("Streaming model: {} with prompt: {}", getType(), request.getPrompt());
            StreamResult response = doStream(request);
            log.debug("Model: {} streaming started", getType());
            return response;
            
        } catch (Exception e) {
            log.error("Error streaming model: {}. Error: {}", getType(), e.getMessage(), e);
            // 返回一个包含错误信息的 StreamResult
            return new StreamResult(Flux.error(new RuntimeException("Failed to stream model: " + getType().name(), e)));
        }
    }
}
