package com.shiyu.ai.common.core.utils.reflect;

import cn.hutool.core.util.ReflectUtil;

import com.shiyu.ai.common.core.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * 提供 Reflect 相关的通用辅助操作，供业务和基础设施复用。
 */
@SuppressWarnings("rawtypes")
public class ReflectUtils extends ReflectUtil {

    /**
     * SETTER_PREFIX 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String SETTER_PREFIX = "set";

    /**
     * GETTER_PREFIX 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String GETTER_PREFIX = "get";

    /** 调用Getter方法. 支持多级，如：对象名.对象名.方法 */
    @SuppressWarnings("unchecked")
    public static <E> E invokeGetter(Object obj, String propertyName) {
        Object object = obj;
        for (String name : StringUtils.split(propertyName, ".")) {
            String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
            object = invoke(object, getterMethodName);
        }
        return (E) object;
    }

    /** 调用Setter方法, 仅匹配方法名。 支持多级，如：对象名.对象名.方法 */
    public static <E> void invokeSetter(Object obj, String propertyName, E value) {
        Object object = obj;
        String[] names = StringUtils.split(propertyName, ".");
        for (int i = 0; i < names.length; i++) {
            if (i < names.length - 1) {
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(names[i]);
                object = invoke(object, getterMethodName);
            } else {
                String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(names[i]);
                Method method = getMethodByName(object.getClass(), setterMethodName);
                invoke(object, method, value);
            }
        }
    }
}
