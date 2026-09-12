package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.FilterOperatorEnum;

import lombok.Data;

/** 过滤条件实体类 */
@Data
public class FilterCondition {
    /** 过滤字段名 */
    private String field;

    /** 过滤操作符 (eq, ne, gt, ge, lt, le, like, in 等) */
    private FilterOperatorEnum operator;

    /** 过滤值 */
    private Object value;

    /**
     * {@code FilterCondition} 创建并初始化当前类型实例。
     */
    public FilterCondition() {}

    /**
     * {@code FilterCondition} 创建并初始化当前类型实例。
     *
     * @param field 参数值，用于执行当前操作。
     * @param operator 参数值，用于执行当前操作。
     * @param value 参数值，用于执行当前操作。
     */
    public FilterCondition(String field, FilterOperatorEnum operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    /**
     * {@code FilterCondition} 创建并初始化当前类型实例。
     *
     * @param field 参数值，用于执行当前操作。
     * @param operatorCode 参数值，用于执行当前操作。
     * @param value 参数值，用于执行当前操作。
     */
    public FilterCondition(String field, String operatorCode, Object value) {
        this.field = field;
        this.operator = FilterOperatorEnum.fromCode(operatorCode);
        this.value = value;
    }
}
