package com.shiyu.ai.common.core.service;

/**
 * @description: 基于ThreadLocal封装工具类
 **/
public class BaseContext {
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void setCurrentToken(String token){
        threadLocal.set(token);
    }

    public static String getCurrentToken(){
        return threadLocal.get();
    }
}