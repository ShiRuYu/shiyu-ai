package com.shiyu.ai.governance.contract;

/**
 * 封装 Quota Decision 相关的不可变数据及其字段约束。
 */
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
