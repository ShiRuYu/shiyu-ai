package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum MenuTypeEnum {
    DIRECTORY("目录"),
    MENU("菜单"),
    BUTTON("按钮");

    private final String name;

    /**
     * 根据名称获取枚举
     */
    public static MenuTypeEnum fromName(String name) {
        return Arrays.stream(values())
                .filter(menuTypeEnum -> menuTypeEnum.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isDirectory() {
        return this == DIRECTORY;
    }

    public boolean isMenu() {
        return this == MENU;
    }

    public boolean isButton() {
        return this == BUTTON;
    }
}

