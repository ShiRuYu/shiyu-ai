package com.shiyu.ai.common.core.exception;

import java.io.Serial;

public class AuthException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }
}
