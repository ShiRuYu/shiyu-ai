package com.shiyu.ai.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 定义 Filter Operator Enum 可用的枚举值及其业务语义。
 */
@Getter
@AllArgsConstructor
public enum FilterOperatorEnum {
    EQ("eq", "="), // 等于
    NE("ne", "!="), // 不等于
    GT("gt", ">"), // 大于
    GE("ge", ">="), // 大于等于
    LT("lt", "<"), // 小于
    LE("le", "<="), // 小于等于
    LIKE("like", "LIKE"), // 模糊匹配
    IN("in", "IN"), // 包含
    NOT_IN("not_in", "NOT IN"), // 不包含
    IS_NULL("is_null", "IS NULL"), // 为空
    IS_NOT_NULL("is_not_null", "IS NOT NULL"); // 不为空

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * sqlOperator 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String sqlOperator;

    /**
     * 执行 Filter Operator Enum 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Filter Operator Enum 相关操作生成的结果数据。
     */
    public static FilterOperatorEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(operator -> operator.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown filter operator: " + code));
    }
}
