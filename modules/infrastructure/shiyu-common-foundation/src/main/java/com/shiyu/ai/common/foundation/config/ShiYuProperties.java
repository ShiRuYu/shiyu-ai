package com.shiyu.ai.common.foundation.config;

import com.shiyu.ai.common.foundation.factory.YmlPropertySourceFactory;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 定义 Shi Yu 基础设施或应用能力的配置项及装配规则。
 */
@Data
@Component
@ConfigurationProperties(prefix = "shiyu")
@PropertySource(value = "classpath:shiyu-common.yml", factory = YmlPropertySourceFactory.class)
public class ShiYuProperties {

    /** 项目名称 */
    private String name;

    /** 版本 */
    private String version;

    /** 版权年份 */
    private String copyrightYear;

    /** 实例演示开关 */
    private boolean demoEnabled;

    /** 获取地址开关 */
    private boolean addressEnabled;
}
