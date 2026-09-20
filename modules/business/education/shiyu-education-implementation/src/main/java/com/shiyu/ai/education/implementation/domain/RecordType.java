package com.shiyu.ai.education.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 定义 Record Type 可用的枚举值及其业务语义。
 */
@Getter
@AllArgsConstructor
public enum RecordType {
    LEARN("LEARN", "学习"),
    PRACTICE("PRACTICE", "练习"),
    REVIEW("REVIEW", "复习"),
    EXAM("EXAM", "考试");

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
}
