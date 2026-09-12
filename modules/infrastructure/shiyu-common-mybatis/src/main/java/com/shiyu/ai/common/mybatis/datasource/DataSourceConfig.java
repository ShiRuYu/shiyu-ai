package com.shiyu.ai.common.mybatis.datasource;

/**
 * DataSourceConfig 配置组件，负责注册和配置基础设施领域相关基础设施。
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
