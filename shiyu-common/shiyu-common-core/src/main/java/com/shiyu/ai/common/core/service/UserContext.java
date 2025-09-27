package com.shiyu.ai.common.core.service;

import com.shiyu.ai.common.core.domain.LoginUser;

public class UserContext {

    private static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    public static void setLoginUser(LoginUser loginUser){
        threadLocal.set(loginUser);
    }

    public static LoginUser getLoginUser(){
        return threadLocal.get();
    }
}
