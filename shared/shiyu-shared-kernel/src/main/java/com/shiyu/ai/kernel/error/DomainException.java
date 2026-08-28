package com.shiyu.ai.kernel.error;

import java.util.Objects;

/** Base exception carrying a stable business error code. */
public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = requireCode(code);
    }

    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = requireCode(code);
    }

    public final String code() {
        return code;
    }

    private static String requireCode(String code) {
        Objects.requireNonNull(code, "code must not be null");
        if (code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        return code;
    }
}
