package com.shiyu.ai.common.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义 Xss 相关的协作契约和调用边界。
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
     * 执行 Xss 相关业务数据，并返回处理结果。
     *
     * @return 返回 Xss 相关操作生成的结果数据。
     */
    String message() default "不允许任何脚本运行";

    /**
     * 执行 Xss 相关业务数据，并返回处理结果。
     *
     * @return 返回 Xss 相关操作生成的结果数据。
     */
    Class<?>[] groups() default {};

    /**
     * 执行 Xss 相关业务数据，并返回处理结果。
     *
     * @return 返回 Xss 相关操作生成的结果数据。
     */
    Class<? extends Payload>[] payload() default {};
}
