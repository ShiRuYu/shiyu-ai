package com.shiyu.ai.common.foundation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 定义 用户 Type Enum 可用的枚举值及其业务语义。
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    /** pc端 */
    SYS_USER("sys_user"),

    /** app端 */
    APP_USER("app_user");

    /**
     * 类型，表示当前对象中的对应属性。
     */
    private final String type;

    /** 根据类型获取枚举 */
    public static UserTypeEnum fromName(String type) {
        return Arrays.stream(values())
                .filter(userTypeEnum -> userTypeEnum.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}
