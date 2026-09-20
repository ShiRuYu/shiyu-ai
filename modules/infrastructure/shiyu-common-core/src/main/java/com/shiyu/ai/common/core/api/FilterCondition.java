package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.FilterOperatorEnum;

import lombok.Data;

/**
 * 实现 Filter Condition 相关的业务处理、协作逻辑或基础设施能力。
 */
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
     * 执行 Filter Condition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param field 用于完成本次业务处理的 field 参数。
     * @param operator 用于完成本次业务处理的 operator 参数。
     * @param value 用于完成本次业务处理的 value 参数。
     */
    public FilterCondition(String field, FilterOperatorEnum operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    /**
     * 执行 Filter Condition 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param field 用于完成本次业务处理的 field 参数。
     * @param operatorCode 用于完成本次业务处理的 operatorCode 参数。
     * @param value 用于完成本次业务处理的 value 参数。
     */
    public FilterCondition(String field, String operatorCode, Object value) {
        this.field = field;
        this.operator = FilterOperatorEnum.fromCode(operatorCode);
        this.value = value;
    }
}
