package com.shiyu.ai.common.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code Xss} 是 Web 输入校验注解，用于标记需要进行跨站脚本过滤的参数。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(
        value = {
            ElementType.METHOD,
            ElementType.FIELD,
            ElementType.CONSTRUCTOR,
            ElementType.PARAMETER
        })
@Constraint(validatedBy = {XssValidator.class})
public @interface Xss {

    /**
     * 执行 {@code message} 定义的接口操作。
     *
     * @return 操作结果。
     */
    String message() default "不允许任何脚本运行";

    /**
     * 执行 {@code groups} 定义的接口操作。
     *
     * @return 符合条件的结果集合。
     */
    Class<?>[] groups() default {};

    /**
     * 执行 {@code payload} 定义的接口操作。
     *
     * @return 符合条件的结果集合。
     */
    Class<? extends Payload>[] payload() default {};
}
