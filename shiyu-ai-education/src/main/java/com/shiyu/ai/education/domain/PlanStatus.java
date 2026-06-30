package com.shiyu.ai.education.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanStatus {

    ACTIVE("ACTIVE", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    ABANDONED("ABANDONED", "已放弃");

    private final String code;
    private final String name;
}
