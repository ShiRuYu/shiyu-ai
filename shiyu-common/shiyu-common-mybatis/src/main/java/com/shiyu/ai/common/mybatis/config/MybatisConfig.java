package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    public FlexGlobalConfig MybatisFlexGLobalConfig() {
        FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
        keyConfig.setKeyType(KeyType.Sequence);
        keyConfig.setValue("select SEQ_USER_ID.nextval as id from dual");
        keyConfig.setBefore(true);

        FlexGlobalConfig.getDefaultConfig().setKeyConfig(keyConfig);
        return FlexGlobalConfig.getDefaultConfig();
    }
}
