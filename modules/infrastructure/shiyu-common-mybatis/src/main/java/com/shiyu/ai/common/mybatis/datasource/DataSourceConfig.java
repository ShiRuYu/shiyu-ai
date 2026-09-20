package com.shiyu.ai.common.mybatis.datasource;

/**
 * 定义 Data Source 基础设施或应用能力的配置项及装配规则。
 */
public final class DataSourceConfig {

    /**
     * AGENT 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String AGENT = "agent";

    private DataSourceConfig() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }
}
