package com.shiyu.ai.education.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewStatus {

    PENDING("PENDING", "待复习"),
    IN_REVIEW("IN_REVIEW", "复习中"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "未通过"),
    OVERDUE("OVERDUE", "已过期");

    private final String code;
    private final String name;
}
