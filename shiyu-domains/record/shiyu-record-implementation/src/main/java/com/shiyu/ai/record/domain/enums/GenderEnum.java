package com.shiyu.ai.record.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {

    MALE(0, "男"),
    FEMALE(1, "女"),
    UNKNOWN(2, "未知");

    private final Integer code;
    private final String label;

    /**
     * 根据 code 获取枚举
     */
    public static GenderEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (GenderEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据 code 获取 label
     */
    public static String getLabelByCode(Integer code) {
        return fromCode(code).getLabel();
    }
}
