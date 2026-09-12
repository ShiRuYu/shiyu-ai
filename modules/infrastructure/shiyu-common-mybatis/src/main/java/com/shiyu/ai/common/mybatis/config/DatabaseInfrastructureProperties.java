package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Set;

/**
 * DatabaseInfrastructureProperties 配置属性，集中管理基础设施领域相关运行参数。
 */
@ConfigurationProperties(prefix = "shiyu.infrastructure.database")
public class DatabaseInfrastructureProperties {

    private static final Set<String> SUPPORTED = Set.of("h2", "mysql", "postgresql");

    /**
     * 提供者，表示当前对象中的对应属性。
     */
    private String provider = "h2";

    /**
     * {@code getProvider} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getProvider() {
        return provider;
    }

    /**
     * {@code setProvider} 写入或更新当前模块中的业务数据。
     *
     * @param provider 参数值，用于执行当前操作。
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * {@code normalizedProvider} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public String normalizedProvider() {
        return provider == null || provider.isBlank()
                ? "h2"
                : provider.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * {@code validate} 校验当前操作的输入或状态是否满足约束。
     */
    public void validate() {
        String normalized = normalizedProvider();
        if (!SUPPORTED.contains(normalized)) {
            throw new IllegalArgumentException(
                    "不支持的数据库 provider: " + provider + "，可用类型: " + SUPPORTED);
        }
    }
}
