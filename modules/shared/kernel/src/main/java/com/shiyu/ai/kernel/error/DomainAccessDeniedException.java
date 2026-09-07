package com.shiyu.ai.kernel.error;

/** Authorization failure safe to map to HTTP 403. */
public final class DomainAccessDeniedException extends DomainException {

    private static final long serialVersionUID = 1L;

    public DomainAccessDeniedException(String code, String message) {
        super(code, message);
    }
}
