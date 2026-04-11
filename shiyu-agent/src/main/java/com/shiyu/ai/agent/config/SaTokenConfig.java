package com.shiyu.ai.agent.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 配置类
 * 配置 JWT 模式、单设备登录等
 */
@Configuration
public class SaTokenConfig {

    /**
     * Sa-Token 整合 JWT (简单模式)
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}
