package com.shiyu.ai.common.storage.rate;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code LocalRateLimitStore} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
@ConditionalOnProperty(
        prefix = "shiyu.infrastructure.redis",
        name = "provider",
        havingValue = "disabled",
        matchIfMissing = true)
public class LocalRateLimitStore implements RateLimitStore {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * {@code consume} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     * @param permits 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     * @param window 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean consume(String key, long permits, long limit, Duration window) {
        long now = System.nanoTime();
        Window next =
                windows.compute(
                        key,
                        (k, old) ->
                                old == null || old.expiresAt < now
                                        ? new Window(now + window.toNanos(), permits)
                                        : new Window(old.expiresAt, old.used + permits));
        return next.used <= limit;
    }

    /**
     * {@code Window} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param expiresAt 过期时间，表示该记录组件承载的数据。
     * @param used used 属性，表示该记录组件承载的数据。
     */
    private record Window(long expiresAt, long used) {}
}
