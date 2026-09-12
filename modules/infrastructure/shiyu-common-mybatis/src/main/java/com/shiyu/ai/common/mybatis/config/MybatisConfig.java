package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import com.shiyu.ai.common.mybatis.handler.AuditFieldListener;
import com.shiyu.ai.common.mybatis.handler.MybatisExceptionHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * {@code MybatisConfig} 提供平台基础设施模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration
@PropertySource(
        value = "classpath:application-common-mybatis.yml",
        factory = YmlPropertySourceFactory.class)
public class MybatisConfig {

    /**
     * {@code mybatisFlexCustomizer} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public MyBatisFlexCustomizer mybatisFlexCustomizer() {
        return config -> {
            AuditFieldListener auditListener = new AuditFieldListener();
            config.registerInsertListener(auditListener, Object.class);
            config.registerUpdateListener(auditListener, Object.class);
        };
    }

    /**
     * {@code mybatisExceptionHandler} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public MybatisExceptionHandler mybatisExceptionHandler() {
        return new MybatisExceptionHandler();
    }
}
