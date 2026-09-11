package com.shiyu.ai.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiyu.ai.common.storage.config.RedisInfrastructureProperties;
import com.shiyu.ai.common.storage.idempotency.RedisIdempotencyStore;
import com.shiyu.ai.common.storage.lease.RedisLeaseStore;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

/** Runs only when Docker is available; CI executes this against real Redis. */
@Testcontainers(disabledWithoutDocker = true)
class RedisStoreContainerTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redis;

    @BeforeAll
    static void startRedis() {
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();
        redis = new StringRedisTemplate(connectionFactory);
        redis.afterPropertiesSet();
    }

    @Test
    void supportsLeaseRenewReleaseAndIdempotency() {
        RedisInfrastructureProperties properties = new RedisInfrastructureProperties();
        properties.setKeyPrefix("container-test");
        RedisLeaseStore lease = new RedisLeaseStore(redis, properties);
        RedisIdempotencyStore idempotency = new RedisIdempotencyStore(redis, properties);

        assertThat(lease.tryAcquire("job", "owner-a", Duration.ofSeconds(10))).isTrue();
        assertThat(lease.tryAcquire("job", "owner-b", Duration.ofSeconds(10))).isFalse();
        assertThat(lease.renew("job", "owner-a", Duration.ofSeconds(10))).isTrue();
        lease.release("job", "owner-a");
        assertThat(lease.tryAcquire("job", "owner-b", Duration.ofSeconds(10))).isTrue();

        assertThat(idempotency.putIfAbsent("request-1", Duration.ofSeconds(10))).isTrue();
        assertThat(idempotency.putIfAbsent("request-1", Duration.ofSeconds(10))).isFalse();
        assertThat(idempotency.contains("request-1")).isTrue();
    }
}
