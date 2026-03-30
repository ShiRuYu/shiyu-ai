package com.shiyu.ai.agent.service;

import java.util.Map;

/**
 * 验证码服务
 * 提供验证码生成、验证和销毁功能
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     * @return 验证码信息（包含 key 和 SVG 图片）
     */
    Map<String, Object> generateCaptcha();
    
    /**
     * 验证验证码
     * @param key 验证码 key
     * @param code 用户输入的验证码
     * @return 验证结果
     */
    boolean validateCaptcha(String key, String code);
    
    /**
     * 销毁验证码（使用后立即销毁）
     * @param key 验证码 key
     */
    void destroyCaptcha(String key);
}
