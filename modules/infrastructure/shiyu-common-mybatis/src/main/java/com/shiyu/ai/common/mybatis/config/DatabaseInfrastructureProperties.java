package com.shiyu.ai.common.mybatis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.Set;

/**
 * 定义 数据库 Infrastructure 基础设施或应用能力的配置项及装配规则。
 */
@ConfigurationProperties(prefix = "shiyu.infrastructure.database")
@Getter
@Setter
public class DatabaseInfrastructureProperties {

    private static final Set<String> SUPPORTED = Set.of("h2", "mysql", "postgresql");

    /**
     * 提供者，表示当前对象中的对应属性。
     */
    private String provider = "h2";

    /**
     * 执行 数据库 Infrastructure 相关业务数据，并返回处理结果。
     *
     * @return 返回 数据库 Infrastructure 相关操作生成的结果数据。
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
