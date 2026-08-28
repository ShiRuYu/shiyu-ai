package com.shiyu.ai.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 教育模块路由前缀配置
 *
 * 给 Education Web 适配器包下所有 Controller 统一添加 /api/education 前缀，
 * 无需在每个 Controller 的 @RequestMapping 中手工加 /api/education。
 *
 * 例如：/chapter/detail → /api/education/chapter/detail
 *       /subject/list   → /api/education/subject/list
 */
@Configuration
public class EduWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/education", clazz ->
            clazz.getPackageName().startsWith("com.shiyu.ai.education.web")
                || clazz.getPackageName().startsWith("com.shiyu.ai.web.education")
        );
    }
}
