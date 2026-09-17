package com.shiyu.ai.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

/**
 * RedisInfrastructureProperties 配置属性，集中管理基础设施领域相关运行参数。
 */
@ConfigurationProperties(prefix = "shiyu.infrastructure.redis")
@Getter
@Setter
public class RedisInfrastructureProperties {

    /**
     * 提供者，表示当前对象中的对应属性。
     */
    private String provider = "disabled";
    /**
     * 地址，表示当前对象中的对应属性。
     */
    private String url = "redis://127.0.0.1:6379/0";
    /**
     * password 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String password;
    /**
     * keyPrefix 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String keyPrefix = "shiyu";

    /**
     * {@code normalizedProvider} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public String normalizedProvider() {
        return provider == null || provider.isBlank()
                ? "disabled"
                : provider.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * {@code validate} 校验当前操作的输入或状态是否满足约束。
     */
    public void validate() {
        String selected = normalizedProvider();
        if (!"disabled".equals(selected) && !"redis".equals(selected)) {
            throw new IllegalStateException(
                    "Unsupported Redis provider: " + selected + "; expected disabled or redis");
        }
        if ("redis".equals(selected) && (url == null || url.isBlank())) {
            throw new IllegalStateException(
                    "Redis provider requires shiyu.infrastructure.redis.url");
        }
    }

    /**
     * {@code key} 执行当前类型定义的业务操作。
     *
     * @param parts 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public String key(String... parts) {
        String prefix = keyPrefix == null || keyPrefix.isBlank() ? "shiyu" : keyPrefix.trim();
        StringBuilder key = new StringBuilder(prefix);
        for (String part : parts) {
            if (part != null && !part.isBlank()) key.append(':').append(part);
        }
        return key.toString();
    }
}
