package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.time.Duration;

/**
 * RateLimitStore 接口，定义基础设施模块的能力边界。
 */
public interface RateLimitStore {
    /**
     * 执行 {@code consume} 定义的接口操作。
     *
     * @param key 方法参数。
     * @param permits 方法参数。
     * @param limit 方法参数。
     * @param window 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean consume(String key, long permits, long limit, Duration window);
}
