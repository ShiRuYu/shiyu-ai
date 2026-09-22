package com.shiyu.ai.common.foundation.utils;

import cn.hutool.core.util.ObjectUtil;

import java.util.function.Function;

/**
 * 提供 Object 相关的通用辅助操作，供业务和基础设施复用。
 */
public class ObjectUtils extends ObjectUtil {
    /**
     * 获取并校验 Object 相关业务数据，并返回处理结果。
     *
     * @param obj 用于完成本次业务处理的 obj 参数。
     * @return 返回 Object 相关操作生成的结果数据。
     */
    public static <T> T requireNonNull(T obj) {
        if (obj == null) throw new NullPointerException();
        return obj;
    }

    /**
     * 如果对象不为空，则获取对象中的某个字段 ObjectUtils.notNullGetter(user, User::getName);
     *
     * @param obj 对象
     * @param func 获取方法
     * @return 对象字段
     */
    public static <T, E> E notNullGetter(T obj, Function<T, E> func) {
        if (isNotNull(obj) && isNotNull(func)) {
            return func.apply(obj);
        }
        return null;
    }

    /**
     * 如果对象不为空，则获取对象中的某个字段，否则返回默认值
     *
     * @param obj 对象
     * @param func 获取方法
     * @param defaultValue 默认值
     * @return 对象字段
     */
    public static <T, E> E notNullGetter(T obj, Function<T, E> func, E defaultValue) {
        if (isNotNull(obj) && isNotNull(func)) {
            return func.apply(obj);
        }
        return defaultValue;
    }

    /**
     * 如果值不为空，则返回值，否则返回默认值
     *
     * @param obj 对象
     * @param defaultValue 默认值
     * @return 对象字段
     */
    public static <T> T notNull(T obj, T defaultValue) {
        if (isNotNull(obj)) {
            return obj;
        }
        return defaultValue;
    }
}
