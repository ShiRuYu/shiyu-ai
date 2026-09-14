package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 用户上下文视图对象 */
@Data
public class UserContextVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;
    /**
     * username 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String username;
    /**
     * 用户类型，表示当前对象中的对应属性。
     */
    private String userType;
    /**
     * 令牌，表示当前对象中的对应属性。
     */
    private String token;
    /**
     * loginTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long loginTime;
    /**
     * expireTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long expireTime;
    /**
     * ipaddr 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String ipaddr;
    /**
     * loginLocation 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String loginLocation;
    /**
     * browser 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String browser;
    /**
     * os 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String os;
    /**
     * nickName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nickName;
    /**
     * avatar 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String avatar;
    /**
     * extInfo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String extInfo;
    /**
     * isLogin 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Boolean isLogin;
    /**
     * 消息，表示当前对象中的对应属性。
     */
    private String message;
    /**
     * deviceInfo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String deviceInfo;
}
