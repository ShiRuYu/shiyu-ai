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
 * 管理 Rate Limit 相关的运行时状态、注册信息或临时数据。
 */
public interface RateLimitStore {
    /**
     * 处理 Rate Limit 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param permits 用于完成本次业务处理的 permits 参数。
     * @param limit 每页返回的数据数量。
     * @param window 用于完成本次业务处理的 window 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean consume(String key, long permits, long limit, Duration window);
}
