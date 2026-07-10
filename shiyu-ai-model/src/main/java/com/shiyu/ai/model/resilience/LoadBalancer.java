package com.shiyu.ai.model.resilience;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡器
 * 从多个平台/模型中轮询选择
 */
@Slf4j
public class LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 轮询选择一个可用平台
     * @param platforms 可用平台列表
     * @param <T> 平台类型
     * @return 选中的平台
     */
    public <T> T roundRobinSelect(List<T> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            throw new IllegalStateException("无可用的平台");
        }
        int index = Math.abs(counter.getAndIncrement() % platforms.size());
        return platforms.get(index);
    }
}
