package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 Captcha 操作向调用方返回的传输数据。
 */
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
     * 执行 Captcha 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param image 用于完成本次业务处理的 image 参数。
     * @param expireTime 用于完成本次业务处理的 expireTime 参数。
     */
    public CaptchaVO(String key, String image, Long expireTime) {
        this.key = key;
        this.image = image;
        this.expireTime = expireTime;
    }
}
