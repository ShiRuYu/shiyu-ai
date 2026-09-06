package com.shiyu.ai.conversation;

/** Stable error surfaced when a generation cannot be admitted by quota policy. */
public class GenerationAdmissionException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    private final String errorCode;
    public GenerationAdmissionException(String errorCode) {
        super(errorCode == null || errorCode.isBlank() ? "generation admission denied" : errorCode);
        this.errorCode = errorCode;
    }
    public String errorCode() { return errorCode; }
}
