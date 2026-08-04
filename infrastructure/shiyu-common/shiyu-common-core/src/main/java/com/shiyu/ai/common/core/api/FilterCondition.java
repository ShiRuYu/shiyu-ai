package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.FilterOperatorEnum;
import lombok.Data;

/**
 * 过滤条件实体类
 */
@Data
public class FilterCondition {
    /**
     * 过滤字段名
     */
    private String field;

    /**
     * 过滤操作符 (eq, ne, gt, ge, lt, le, like, in 等)
     */
    private FilterOperatorEnum operator;

    /**
     * 过滤值
     */
    private Object value;

    public FilterCondition() {
    }

    public FilterCondition(String field, FilterOperatorEnum operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public FilterCondition(String field, String operatorCode, Object value) {
        this.field = field;
        this.operator = FilterOperatorEnum.fromCode(operatorCode);
        this.value = value;
    }
}