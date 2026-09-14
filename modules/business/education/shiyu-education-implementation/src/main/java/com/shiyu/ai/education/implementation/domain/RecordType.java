package com.shiyu.ai.education.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code RecordType} 表示教育模块中的一组受控业务状态或分类。
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
