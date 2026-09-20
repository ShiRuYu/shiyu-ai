package com.shiyu.ai.common.storage.rate;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 Local Rate Limit 相关的运行时状态、注册信息或临时数据。
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
     * 处理 Local Rate Limit 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param permits 用于完成本次业务处理的 permits 参数。
     * @param limit 每页返回的数据数量。
     * @param window 用于完成本次业务处理的 window 参数。
     * @return 返回本次条件判断是否成立。
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
     * 封装 Window 相关的不可变数据及其字段约束。
     */
    private record Window(long expiresAt, long used) {}
}
