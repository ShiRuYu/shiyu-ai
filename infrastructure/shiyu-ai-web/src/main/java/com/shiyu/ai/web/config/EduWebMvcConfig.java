package com.shiyu.ai.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 教育模块路由前缀配置
 *
 * 给 com.shiyu.ai.web.education 包下所有 Controller 统一添加 /edu 前缀，
 * 无需在每个 Controller 的 @RequestMapping 中手工加 /edu。
 *
 * 例如：/chapter/detail → /edu/chapter/detail
 *       /subject/list   → /edu/subject/list
 */
@Configuration
public class EduWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/edu", clazz ->
            clazz.getPackageName().startsWith("com.shiyu.ai.web.education")
        );
    }
}
