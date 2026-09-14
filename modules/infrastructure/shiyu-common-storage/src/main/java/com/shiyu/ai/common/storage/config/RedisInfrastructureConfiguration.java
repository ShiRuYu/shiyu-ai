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

/**
 * RedisInfrastructureConfiguration 配置组件，负责注册和配置基础设施领域相关基础设施。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisInfrastructureProperties.class)
public class RedisInfrastructureConfiguration {

    /**
     * {@code redisProviderValidator} 执行当前类型定义的业务操作。
     *
     * @param properties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public RedisProviderValidator redisProviderValidator(RedisInfrastructureProperties properties) {
        properties.validate();
        return new RedisProviderValidator();
   }

   /**
    * {@code redisConnectionFactory} 执行当前类型定义的业务操作。
     *
     * @param properties 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
    */
    @Bean(destroyMethod = "destroy")
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.redis",
            name = "provider",
            havingValue = "redis")
   public LettuceConnectionFactory redisConnectionFactory(
           RedisInfrastructureProperties properties) {
        properties.validate();
        URI uri = URI.create(properties.getUrl());
        RedisURI parsed = RedisURI.create(properties.getUrl());
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(
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

   /**
    * {@code stringRedisTemplate} 执行当前类型定义的业务操作。
     *
     * @param connectionFactory 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
    */
   @Bean
   @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.redis",
           name = "provider",
           havingValue = "redis")
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

   /**
    * {@code redisLeaseStore} 执行当前类型定义的业务操作。
     *
     * @param redis 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
    */
    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.redis",
            name = "provider",
            havingValue = "redis")
   public DistributedLeaseStore redisLeaseStore(
           StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        return new RedisLeaseStore(redis, properties);
   }

   /**
    * {@code redisRateLimitStore} 执行当前类型定义的业务操作。
     *
     * @param redis 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
    */
    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.redis",
            name = "provider",
            havingValue = "redis")
   public RateLimitStore redisRateLimitStore(
           StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        return new RedisRateLimitStore(redis, properties);
   }

   /**
    * {@code redisIdempotencyStore} 执行当前类型定义的业务操作。
     *
     * @param redis 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
    */
    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.redis",
            name = "provider",
            havingValue = "redis")
   public IdempotencyStore redisIdempotencyStore(
           StringRedisTemplate redis, RedisInfrastructureProperties properties) {
        return new RedisIdempotencyStore(redis, properties);
    }

    /**
     * {@code RedisProviderValidator} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     */
    public record RedisProviderValidator() {}
}
