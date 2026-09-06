package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/**
 * 业务异常
 */
public final class ServiceException extends BaseBizException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException() {
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Integer code) {
        super(message, code);
    }

    @Override
    public ServiceException setMessage(String message) {
        return (ServiceException) super.setMessage(message);
    }

    @Override
    public ServiceException setDetailMessage(String detailMessage) {
        return (ServiceException) super.setDetailMessage(detailMessage);
    }
}
