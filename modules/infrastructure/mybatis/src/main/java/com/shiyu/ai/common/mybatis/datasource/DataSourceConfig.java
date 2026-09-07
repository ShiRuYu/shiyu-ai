package com.shiyu.ai.common.mybatis.datasource;

/**
 * Names of application data sources used by MyBatis adapters.
 *
 * <p>The name is a shared technical contract so domain implementations can
 * bind their own mappers without importing the legacy central DAL module.</p>
 */
public final class DataSourceConfig {

    public static final String AGENT = "agent";

    private DataSourceConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
