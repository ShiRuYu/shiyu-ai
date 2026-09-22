package com.shiyu.ai.common.foundation.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 定义 Device Type Enum 可用的枚举值及其业务语义。
 */
@Getter
@AllArgsConstructor
public enum DeviceTypeEnum {

    /** pc端 */
    PC("pc"),

    /** app端 */
    APP("app"),

    /** 小程序端 */
    XCX("xcx"),

    /** Windows系统 */
    WINDOWS("windows"),

    /** Mac系统 */
    MAC("mac"),

    /** Linux系统 */
    LINUX("linux"),

    /** Android系统 */
    ANDROID("android"),

    /** iOS系统 */
    IOS("ios"),

    /** 未知设备 */
    UNKNOWN("unknown");

    /**
     * device 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String device;
}
