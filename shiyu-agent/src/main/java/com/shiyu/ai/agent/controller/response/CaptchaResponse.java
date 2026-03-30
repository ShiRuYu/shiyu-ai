package com.shiyu.ai.agent.controller;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应
 */
@Data
@NoArgsConstructor
public class CaptchaResponse {
    
    /**
     * 验证码 key（用于后续验证）
     */
    private String key;
    
    /**
     * 验证码图片（Base64 编码的 SVG）
     */
    private String image;
    
    /**
     * 过期时间（秒）
     */
    private Long expireTime;
    
    public CaptchaResponse(String key, String image, Long expireTime) {
        this.key = key;
        this.image = image;
        this.expireTime = expireTime;
    }
}
