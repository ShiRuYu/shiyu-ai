package com.shiyu.ai.common.core.service;

import com.shiyu.ai.common.core.domain.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 基于ThreadLocal封装工具类
 **/
public class BaseContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void set(String key,Object value){
        CONTEXT.get().put(key, value);
    }
    public static Map<String, Object> get(String key){
        return CONTEXT.get();
    }

    public static LoginUser getLoginUser(){
        return (LoginUser) MapUtils.getObject(CONTEXT.get(), MapKey.LOGIN_USER.name());
    }

    public static String getCurrentToken(){
        return MapUtils.getString(CONTEXT.get(), MapKey.TOKEN.name());
    }

    @Getter
    @AllArgsConstructor
    public enum MapKey{
        LOGIN_USER(LoginUser.class),
        TOKEN(String.class);

        private final Class<?> clazz;
    }
}