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
 * LeaseStore 接口，定义基础设施模块的能力边界。
 */
public interface LeaseStore {
    /**
     * 执行 {@code tryAcquire} 定义的接口操作。
     *
     * @param key 方法参数。
     * @param owner 方法参数。
     * @param ttl 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean tryAcquire(String key, String owner, Duration ttl);

    /**
     * 执行 {@code renew} 定义的接口操作。
     *
     * @param key 方法参数。
     * @param owner 方法参数。
     * @param ttl 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean renew(String key, String owner, Duration ttl);

    /**
     * 变更当前业务对象的处理状态。
     *
     * @param key 方法参数。
     * @param owner 方法参数。
     */
    void release(String key, String owner);
}
