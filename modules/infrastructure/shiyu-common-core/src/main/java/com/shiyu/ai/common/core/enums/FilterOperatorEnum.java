package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;

/**
 * 过滤操作符枚举
 */
@Getter
@AllArgsConstructor
public enum FilterOperatorEnum {
    EQ("eq", "="),           // 等于
    NE("ne", "!="),          // 不等于
    GT("gt", ">"),           // 大于
    GE("ge", ">="),          // 大于等于
    LT("lt", "<"),           // 小于
    LE("le", "<="),          // 小于等于
    LIKE("like", "LIKE"),    // 模糊匹配
    IN("in", "IN"),          // 包含
    NOT_IN("not_in", "NOT IN"), // 不包含
    IS_NULL("is_null", "IS NULL"), // 为空
    IS_NOT_NULL("is_not_null", "IS NOT NULL"); // 不为空

    private final String code;
    private final String sqlOperator;

    public static FilterOperatorEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(operator -> operator.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown filter operator: " + code));
    }
}