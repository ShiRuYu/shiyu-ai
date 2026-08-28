package com.shiyu.ai.education.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionType {

    CHOICE("CHOICE", "选择题"),
    FILL("FILL", "填空题"),
    SOLVE("SOLVE", "解答题"),
    JUDGE("JUDGE", "判断题"),
    ESSAY("ESSAY", "论述题"),
    EXPERIMENT("EXPERIMENT", "实验题");

    private final String code;
    private final String name;
}
