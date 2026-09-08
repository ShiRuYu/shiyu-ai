package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 设备类型
 * 针对多套 用户体系
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    /**
     * pc端
     */
    SYS_USER("sys_user"),

    /**
     * app端
     */
    APP_USER("app_user");

    private final String type;

    /**
     * 根据类型获取枚举
     */
    public static UserTypeEnum fromName(String type) {
        return Arrays.stream(values())
                .filter(userTypeEnum -> userTypeEnum.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}
