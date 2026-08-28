package com.shiyu.ai.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户上下文视图对象
 */
@Data
public class UserContextVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String userType;
    private String token;
    private Long loginTime;
    private Long expireTime;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
    private String nickName;
    private String avatar;
    private String extInfo;
    private Boolean isLogin;
    private String message;
    private String deviceInfo;
}
