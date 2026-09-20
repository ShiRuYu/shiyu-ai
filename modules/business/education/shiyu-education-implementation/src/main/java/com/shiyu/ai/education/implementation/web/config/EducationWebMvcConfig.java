package com.shiyu.ai.education.implementation.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 定义 教育 Web Mvc 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
public class EducationWebMvcConfig implements WebMvcConfigurer {

    /**
     * 执行 教育 Web Mvc 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param configurer 用于完成本次业务处理的 configurer 参数。
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                "/api/education",
                clazz -> clazz.getPackageName().startsWith("com.shiyu.ai.education.implementation.web.controller"));
    }
}
