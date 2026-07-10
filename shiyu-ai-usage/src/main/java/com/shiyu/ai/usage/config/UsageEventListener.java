package com.shiyu.ai.usage.config;

import com.shiyu.ai.agent.event.ModelCallEvent;
import com.shiyu.ai.usage.collector.UsageCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用量事件监听器
 * 监听 ModelCallEvent，自动记录 Token 用量
 */
@Slf4j
@Component
public class UsageEventListener {

    private final UsageCollector usageCollector;

    public UsageEventListener(UsageCollector usageCollector) {
        this.usageCollector = usageCollector;
    }

    @EventListener
    @Async
    public void onModelCall(ModelCallEvent event) {
        usageCollector.recordUsage(
            event.getPlatform(),
            event.getModel(),
            event.getPromptTokens(),
            event.getCompletionTokens(),
            event.getLatencyMs(),
            null,  // userId
            null   // sessionId
        );
    }
}
