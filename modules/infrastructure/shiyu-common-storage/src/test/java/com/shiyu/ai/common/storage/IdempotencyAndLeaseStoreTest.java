package com.shiyu.ai.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiyu.ai.common.storage.api.DistributedLeaseStore;
import com.shiyu.ai.common.storage.idempotency.LocalIdempotencyStore;
import com.shiyu.ai.common.storage.lease.LocalLeaseStore;

import org.junit.jupiter.api.Test;

import java.time.Duration;

class IdempotencyAndLeaseStoreTest {

    @Test
    void localIdempotencyExpiresAndRejectsDuplicateKeys() throws Exception {
        LocalIdempotencyStore store = new LocalIdempotencyStore();

        assertThat(store.putIfAbsent("request-1", Duration.ofMillis(50))).isTrue();
        assertThat(store.putIfAbsent("request-1", Duration.ofSeconds(1))).isFalse();
        assertThat(store.contains("request-1")).isTrue();
        Thread.sleep(100);
        assertThat(store.contains("request-1")).isFalse();
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
