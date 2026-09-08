package com.shiyu.ai.common.storage.api;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.time.Duration;

/** Replaceable lease boundary. Local implementations may use an in-process lock; P3 can bind Redis/SQL leases. */
public interface LeaseStore {
    boolean tryAcquire(String key, String owner, Duration ttl);
    boolean renew(String key, String owner, Duration ttl);
    void release(String key, String owner);
}
