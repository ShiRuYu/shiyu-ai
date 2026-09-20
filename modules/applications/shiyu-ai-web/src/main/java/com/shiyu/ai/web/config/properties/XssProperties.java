package com.shiyu.ai.web.config.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定义 Xss 基础设施或应用能力的配置项及装配规则。
 */
@Data
@ConfigurationProperties(prefix = "xss")
public class XssProperties {

    /** 过滤开关 */
    private String enabled;

    /** 排除链接（多个用逗号分隔） */
    private String excludes;

    /** 匹配链接 */
    private String urlPatterns;
}
