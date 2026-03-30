package com.shiyu.ai.agent.controller.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应对象
 */
@Data
public class LoginResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private LoginData data;

    /**
     * 原始 URL（用于登录后跳转）
     */
    private String originUrl;

    /**
     * 登录数据
     */
    @lombok.Data
    public static class LoginData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 访问令牌
         */
        private String accessToken;
    }
}
