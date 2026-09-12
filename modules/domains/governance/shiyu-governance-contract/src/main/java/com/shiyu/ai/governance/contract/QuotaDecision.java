package com.shiyu.ai.governance.contract;

/**
 * 表示配额检查结果、错误码和预留记录标识。
 * @param allowed allowed 属性，表示该记录组件承载的数据。
 * @param errorCode errorCode 属性，表示该记录组件承载的数据。
 * @param reservationId reservationId 属性，表示该记录组件承载的数据。
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
