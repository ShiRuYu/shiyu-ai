package com.shiyu.ai.common.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FileInfrastructureProperties 配置属性，集中管理基础设施领域相关运行参数。
 */
@ConfigurationProperties(prefix = "shiyu.infrastructure.file")
public class FileInfrastructureProperties {

    /**
     * 提供者，表示当前对象中的对应属性。
     */
    private String provider;

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
     * {@code resolveProvider} 查询并返回当前操作所需的数据。
     *
     * @param legacyType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public String resolveProvider(String legacyType) {
        return provider == null || provider.isBlank() ? legacyType : provider.trim();
    }
}
