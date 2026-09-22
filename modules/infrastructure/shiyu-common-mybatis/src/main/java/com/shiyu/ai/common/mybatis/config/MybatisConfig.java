package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.shiyu.ai.common.foundation.factory.YmlPropertySourceFactory;
import com.shiyu.ai.common.mybatis.handler.AuditFieldListener;
import com.shiyu.ai.common.mybatis.handler.MybatisExceptionHandler;
import com.shiyu.ai.common.mybatis.handler.TenantConsistencyListener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 定义 Mybatis 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
@PropertySource(
        value = "classpath:application-common-mybatis.yml",
        factory = YmlPropertySourceFactory.class)
public class MybatisConfig {

    /**
     * 执行 Mybatis 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mybatis 相关操作生成的结果数据。
     */
    @Bean
    public MyBatisFlexCustomizer mybatisFlexCustomizer() {
        return config -> {
            AuditFieldListener auditListener = new AuditFieldListener();
            TenantConsistencyListener tenantListener = new TenantConsistencyListener();
            config.registerInsertListener(auditListener, Object.class);
            config.registerUpdateListener(auditListener, Object.class);
            config.registerInsertListener(tenantListener, Object.class);
            config.registerUpdateListener(tenantListener, Object.class);
        };
    }

    /**
     * 执行 Mybatis 相关业务数据，并返回处理结果。
     *
     * @return 返回 Mybatis 相关操作生成的结果数据。
     */
    @Bean
    public MybatisExceptionHandler mybatisExceptionHandler() {
        return new MybatisExceptionHandler();
    }
}
