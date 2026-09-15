package com.shiyu.ai.common.core.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * 仅在指定业务模块被进程配置启用时注册配置类或 Bean。
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
