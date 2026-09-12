package com.shiyu.ai.common.core.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Set;

/** Validator 校验框架工具 */
public class ValidatorUtils {

    private static final Validator VALID = SpringUtils.getBean(Validator.class);

    /**
     * {@code validate} 校验当前操作的输入或状态是否满足约束。
     *
     * @param object 参数值，用于执行当前操作。
     * @param groups 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> void validate(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> validate = VALID.validate(object, groups);
        if (!validate.isEmpty()) {
            throw new ConstraintViolationException("参数校验异常", validate);
        }
    }
}
