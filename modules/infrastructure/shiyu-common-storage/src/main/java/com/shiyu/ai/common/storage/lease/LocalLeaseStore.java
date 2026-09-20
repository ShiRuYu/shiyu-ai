package com.shiyu.ai.common.storage.lease;

import com.shiyu.ai.common.storage.api.DistributedLeaseStore;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 Local Lease 相关的运行时状态、注册信息或临时数据。
 */
@Component
@ConditionalOnProperty(
        prefix = "shiyu.infrastructure.redis",
        name = "provider",
        havingValue = "disabled",
        matchIfMissing = true)
public class LocalLeaseStore implements DistributedLeaseStore {
    private final ConcurrentHashMap<String, Lease> leases = new ConcurrentHashMap<>();

    /**
     * 执行 Local Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    public boolean tryAcquire(String key, String owner, Duration ttl) {
        long expires = System.nanoTime() + ttl.toNanos();
        return leases.compute(
                        key,
                        (k, old) ->
                                old == null || old.expiresAt < System.nanoTime()
                                        ? new Lease(owner, expires)
                                        : old)
                .owner
                .equals(owner);
    }

    /**
     * 执行 Local Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    public boolean renew(String key, String owner, Duration ttl) {
        return leases.computeIfPresent(
                                key,
                                (k, old) ->
                                        old.owner.equals(owner)
                                                ? new Lease(
                                                        owner, System.nanoTime() + ttl.toNanos())
                                                : old)
                        != null
                && owner.equals(leases.get(key).owner);
    }

    /**
     * 执行 Local Lease 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     */
    public void release(String key, String owner) {
        leases.computeIfPresent(key, (k, old) -> old.owner.equals(owner) ? null : old);
    }

    /**
     * 封装 Lease 相关的不可变数据及其字段约束。
     */
    private record Lease(String owner, long expiresAt) {}
}
