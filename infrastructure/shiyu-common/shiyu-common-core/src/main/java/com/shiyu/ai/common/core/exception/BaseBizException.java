package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/**
 * 业务异常基类
 * 提供 message、detailMessage、code 的统一管理
 * GlobalException 和 ServiceException 继承此类
 */
public abstract class BaseBizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private String message;

    private String detailMessage;

    private Integer code;

    protected BaseBizException() {
    }

    protected BaseBizException(String message) {
        this.message = message;
    }

    protected BaseBizException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public BaseBizException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public BaseBizException setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getCode() {
        return code;
    }
}