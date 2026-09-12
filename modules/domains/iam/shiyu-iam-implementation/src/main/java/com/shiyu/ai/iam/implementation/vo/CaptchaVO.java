package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 验证码响应 VO */
@Data
@NoArgsConstructor
public class CaptchaVO {

    /** 验证码 key（用于后续验证） */
    private String key;

    /** 验证码图片（Base64 编码的 SVG） */
    private String image;

    /** 过期时间（秒） */
    private Long expireTime;

    /**
     * {@code CaptchaVO} 创建并初始化当前类型实例。
     *
     * @param key 参数值，用于执行当前操作。
     * @param image 参数值，用于执行当前操作。
     * @param expireTime 参数值，用于执行当前操作。
     */
    public CaptchaVO(String key, String image, Long expireTime) {
        this.key = key;
        this.image = image;
        this.expireTime = expireTime;
    }
}
