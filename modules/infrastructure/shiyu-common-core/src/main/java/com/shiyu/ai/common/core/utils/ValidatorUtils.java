package com.shiyu.ai.common.core.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * 提供 Validator 相关的通用辅助操作，供业务和基础设施复用。
 */
public class ValidatorUtils {

    private static final Validator VALID = SpringUtils.getBean(Validator.class);

    /**
     * 校验或判断 Validator 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param object 用于完成本次业务处理的 object 参数。
     * @param groups 用于完成本次业务处理的 groups 参数。
     * @return 返回 Validator 相关操作生成的结果数据。
     */
    public static <T> void validate(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> validate = VALID.validate(object, groups);
        if (!validate.isEmpty()) {
            throw new ConstraintViolationException("参数校验异常", validate);
        }
    }
}
