package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型
 * 针对一套 用户体系
 */
@Getter
@AllArgsConstructor
public enum DeviceTypeEnum {

    /**
     * pc端
     */
    PC("pc"),

    /**
     * app端
     */
    APP("app"),

    /**
     * 小程序端
     */
    XCX("xcx"),

    /**
     * Windows系统
     */
    WINDOWS("windows"),

    /**
     * Mac系统
     */
    MAC("mac"),

    /**
     * Linux系统
     */
    LINUX("linux"),

    /**
     * Android系统
     */
    ANDROID("android"),

    /**
     * iOS系统
     */
    IOS("ios"),

    /**
     * 未知设备
     */
    UNKNOWN("unknown");


    private final String device;
}
