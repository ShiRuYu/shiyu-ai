package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum {
    /**
     * 正常
     */
    OK("1", "正常"),
    /**
     * 停用
     */
    DISABLE("0", "停用"),
    /**
     * 删除
     */
    DELETED("2", "删除");

    private final String code;
    private final String info;

}
