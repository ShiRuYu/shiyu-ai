package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.time.Duration;

/**
 * 管理 Lease 相关的运行时状态、注册信息或临时数据。
 */
public interface LeaseStore {
    /**
     * 执行 Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean tryAcquire(String key, String owner, Duration ttl);

    /**
     * 执行 Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean renew(String key, String owner, Duration ttl);

    /**
     * 执行 Lease 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param owner 用于完成本次业务处理的 owner 参数。
     */
    void release(String key, String owner);
}
