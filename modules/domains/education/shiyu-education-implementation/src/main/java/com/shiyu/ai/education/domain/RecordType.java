package com.shiyu.ai.education.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordType {

    LEARN("LEARN", "学习"),
    PRACTICE("PRACTICE", "练习"),
    REVIEW("REVIEW", "复习"),
    EXAM("EXAM", "考试");

    private final String code;
    private final String name;
}
