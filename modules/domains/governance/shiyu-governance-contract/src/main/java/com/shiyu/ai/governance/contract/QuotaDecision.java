package com.shiyu.ai.governance.contract;

/** Stable result for quota admission. */
public record QuotaDecision(boolean allowed, String errorCode, long reservationId) {
    public QuotaDecision {
        if (allowed && reservationId <= 0) {
            throw new IllegalArgumentException("an allowed decision must have a reservation id");
        }
        if (!allowed && reservationId != 0) {
            throw new IllegalArgumentException("a denied decision must not have a reservation id");
        }
        if (!allowed && (errorCode == null || errorCode.isBlank())) {
            throw new IllegalArgumentException("a denied decision must have an error code");
        }
    }
}
