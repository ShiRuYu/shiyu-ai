package com.shiyu.ai.observation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 可观测性自动配置
 *
 * <p>提供审计日志、执行时间线、Metrics 聚合等能力的自动装配。
 * 当前为基础骨架，审计/时间线服务在 shiyu-ai-agent 模块中实现，
 * 后续将逐步迁移至此模块。
 */
@Slf4j
@Configuration
public class ObservationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObservationMarker observationMarker() {
        log.info("Observation 模块已加载");
        return new ObservationMarker();
    }

    /**
     * 标记 Bean，确保自动配置被 Spring 扫描到
     */
    public static class ObservationMarker {
    }
}
