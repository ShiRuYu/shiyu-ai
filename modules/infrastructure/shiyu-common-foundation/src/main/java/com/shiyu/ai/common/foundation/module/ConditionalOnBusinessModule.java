package com.shiyu.ai.common.foundation.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 定义 Conditional On Business Module 相关的协作契约和调用边界。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(BusinessModuleCondition.class)
public @interface ConditionalOnBusinessModule {

    /**
     * 返回模块稳定标识，用于拼接配置键。
     *
     * @return 模块标识。
     */
    String value();

    /**
     * 返回未配置开关时是否启用该模块。
     *
     * @return 缺省启用状态。
     */
    boolean matchIfMissing() default false;
}
