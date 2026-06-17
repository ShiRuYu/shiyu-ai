package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import com.shiyu.ai.common.mybatis.handler.AuditFieldListener;
import com.shiyu.ai.common.mybatis.handler.MybatisExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application-common-mybatis.yml", factory = YmlPropertySourceFactory.class)
public class MybatisConfig {

    @Bean
    public MyBatisFlexCustomizer mybatisFlexCustomizer() {
        return config -> {
            AuditFieldListener auditListener = new AuditFieldListener();
            config.registerInsertListener(auditListener, Object.class);
            config.registerUpdateListener(auditListener, Object.class);
        };
    }

    @Bean
    public MybatisExceptionHandler mybatisExceptionHandler() {
        return new MybatisExceptionHandler();
    }
}
