package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.shiyu.ai.common.mybatis.handler.AuditFieldListener;
import com.shiyu.ai.common.mybatis.handler.MybatisExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Value("${mybatis-flex.key-type:Auto}")
    private String keyType;

    @Value("${mybatis-flex.key-value:}")
    private String keyValue;

    @Value("${mybatis-flex.key-before:true}")
    private boolean keyBefore;

    @Bean
    public FlexGlobalConfig MybatisFlexGLobalConfig() {
        String resolvedKeyType = keyType;
        if (resolvedKeyType == null || resolvedKeyType.isBlank()) {
            resolvedKeyType = "Auto";
        }
        FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
        keyConfig.setKeyType(KeyType.valueOf(resolvedKeyType));
        if (keyValue != null && !keyValue.isBlank()) {
            keyConfig.setValue(keyValue);
        }
        keyConfig.setBefore(keyBefore);

        FlexGlobalConfig config = FlexGlobalConfig.getDefaultConfig();
        config.setKeyConfig(keyConfig);

        // 注册审计字段自动填充监听器
        AuditFieldListener auditListener = new AuditFieldListener();
        config.registerInsertListener(auditListener, Object.class);
        config.registerUpdateListener(auditListener, Object.class);

        return config;
    }

    @Bean
    public MybatisExceptionHandler mybatisExceptionHandler() {
        return new MybatisExceptionHandler();
    }
}
