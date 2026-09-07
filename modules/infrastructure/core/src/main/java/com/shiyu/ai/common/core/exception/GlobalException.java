package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/**
 * 全局异常
 */
public class GlobalException extends BaseBizException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GlobalException() {
    }

    public GlobalException(String message) {
        super(message);
    }
}
