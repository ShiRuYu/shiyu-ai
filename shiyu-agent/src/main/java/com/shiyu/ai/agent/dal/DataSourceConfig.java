package com.shiyu.ai.agent.dal;

/**
 * 数据源配置常量类
 * 用于多数据源配置标识
 */
public final class DataSourceConfig {
    
    /**
     * auth (认证数据源)
     */
    public static final String AUTH = "auth";
    
    /**
     * record (个人成长记录系统数据源)
     */
    public static final String RECORD = "record";
    
    /**
     * 私有构造函数,防止实例化
     */
    private DataSourceConfig() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
