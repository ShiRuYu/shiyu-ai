package com.shiyu.ai.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 定义 文件 Infrastructure 基础设施或应用能力的配置项及装配规则。
 */
@ConfigurationProperties(prefix = "shiyu.infrastructure.file")
@Getter
@Setter
public class FileInfrastructureProperties {

    /**
     * 提供者，表示当前对象中的对应属性。
     */
    private String provider;

    /**
     * 解析或路由 文件 Infrastructure 相关业务数据，并返回处理结果。
     *
     * @param legacyType 用于完成本次业务处理的 legacyType 参数。
     * @return 返回 文件 Infrastructure 相关操作生成的结果数据。
     */
    public String resolveProvider(String legacyType) {
        return provider == null || provider.isBlank() ? legacyType : provider.trim();
    }
}
