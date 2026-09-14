package com.shiyu.ai.education.implementation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code QuestionType} 表示教育模块中的一组受控业务状态或分类。
 */
@Getter
@AllArgsConstructor
public enum QuestionType {
    CHOICE("CHOICE", "选择题"),
    FILL("FILL", "填空题"),
    SOLVE("SOLVE", "解答题"),
    JUDGE("JUDGE", "判断题"),
    ESSAY("ESSAY", "论述题"),
    EXPERIMENT("EXPERIMENT", "实验题");

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
}
