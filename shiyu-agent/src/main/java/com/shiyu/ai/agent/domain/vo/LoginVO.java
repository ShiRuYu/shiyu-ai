package com.shiyu.ai.agent.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应 VO
 */
@Data
public class LoginVO implements Serializable {

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
    private LoginDataVO data;

    /**
     * 原始 URL（用于登录后跳转）
     */
    private String originUrl;

    /**
     * 登录数据 VO
     */
    @lombok.Data
    public static class LoginDataVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 访问令牌
         */
        private String accessToken;
    }
}
