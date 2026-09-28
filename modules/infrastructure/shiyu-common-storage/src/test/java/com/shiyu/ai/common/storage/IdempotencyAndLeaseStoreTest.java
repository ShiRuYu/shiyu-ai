package com.shiyu.ai.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiyu.ai.common.storage.api.DistributedLeaseStore;
import com.shiyu.ai.common.storage.idempotency.LocalIdempotencyStore;
import com.shiyu.ai.common.storage.lease.LocalLeaseStore;

import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * 验证 Idempotency And Lease Store 相关功能、边界条件、异常路径和协作行为。
 */
class IdempotencyAndLeaseStoreTest {

    @Test
    void localIdempotencyExpiresAndRejectsDuplicateKeys() throws Exception {
        LocalIdempotencyStore store = new LocalIdempotencyStore();
        Duration ttl = Duration.ofSeconds(1);

        assertThat(store.putIfAbsent("request-1", ttl)).isTrue();
        assertThat(store.putIfAbsent("request-1", Duration.ofSeconds(1))).isFalse();
        assertThat(store.contains("request-1")).isTrue();

        long deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
        boolean expired = false;
        while (System.nanoTime() < deadline) {
            if (!store.contains("request-1")) {
                expired = true;
                break;
            }
            Thread.sleep(10);
        }
        assertThat(expired).isTrue();
    }

    @Test
    void distributedLeaseConvenienceMethodsUseTheExistingLeaseContract() {
        DistributedLeaseStore store = new LocalLeaseStore();

        assertThat(store.acquire("resource", Duration.ofSeconds(1))).isTrue();
        assertThat(store.renew("resource", Duration.ofSeconds(1))).isTrue();
        store.release("resource");
        assertThat(store.acquire("resource", Duration.ofSeconds(1))).isTrue();
    }
}
