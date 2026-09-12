package com.shiyu.ai.education.implementation.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * EducationWebMvcConfig 配置组件，负责注册和配置教育领域相关基础设施。
 */
@Configuration
public class EducationWebMvcConfig implements WebMvcConfigurer {

    /**
     * {@code configurePathMatch} 执行当前类型定义的业务操作。
     *
     * @param configurer 参数值，用于执行当前操作。
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                "/api/education",
                clazz -> clazz.getPackageName().startsWith("com.shiyu.ai.education.implementation.web.controller"));
    }
}
