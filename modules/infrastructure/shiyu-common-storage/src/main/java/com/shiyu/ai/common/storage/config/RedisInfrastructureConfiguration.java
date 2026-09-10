package com.shiyu.ai.common.storage.config;

import com.shiyu.ai.common.storage.api.DistributedLeaseStore;
import com.shiyu.ai.common.storage.api.IdempotencyStore;
import com.shiyu.ai.common.storage.api.RateLimitStore;
import com.shiyu.ai.common.storage.idempotency.RedisIdempotencyStore;
import com.shiyu.ai.common.storage.lease.RedisLeaseStore;
import com.shiyu.ai.common.storage.rate.RedisRateLimitStore;
import io.lettuce.core.RedisURI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.URI;

/** Enables Redis only when the selected provider is explicitly redis. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisInfrastructureProperties.class)
public class RedisInfrastructureConfiguration {

    @Bean
    public RedisProviderValidator redisProviderValidator(RedisInfrastructureProperties properties) {
        properties.validate();
        return new RedisProviderValidator();
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnProperty(prefix = "shiyu.infrastructure.redis", name = "provider", havingValue = "redis")
    public LettuceConnectionFactory redisConnectionFactory(RedisInfrastructureProperties properties) {
        properties.validate();
        URI uri = URI.create(properties.getUrl());
        RedisURI parsed = RedisURI.create(properties.getUrl());
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 6379);
        configuration.setDatabase(Math.max(0, parsed.getDatabase()));
        String password = properties.getPassword();
        if (password == null && uri.getUserInfo() != null && uri.getUserInfo().contains(":")) {
            password = uri.getUserInfo().substring(uri.getUserInfo().indexOf(':') + 1);
        }
        if (password != null && !password.isBlank()) {
            configuration.setPassword(RedisPassword.of(password));
        }
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @ConditionalOnProperty(prefix = "shiyu.infrastructure.redis", name = "provider", havingValue = "redis")
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "shiyu.infrastructure.redis", name = "provider", havingValue = "redis")
    public DistributedLeaseStore redisLeaseStore(StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        return new RedisLeaseStore(redis, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "shiyu.infrastructure.redis", name = "provider", havingValue = "redis")
    public RateLimitStore redisRateLimitStore(StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        return new RedisRateLimitStore(redis, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "shiyu.infrastructure.redis", name = "provider", havingValue = "redis")
    public IdempotencyStore redisIdempotencyStore(StringRedisTemplate redis,
                                                  RedisInfrastructureProperties properties) {
        return new RedisIdempotencyStore(redis, properties);
    }

    public record RedisProviderValidator() {
    }
}
