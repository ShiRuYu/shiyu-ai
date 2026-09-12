package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.SortDirectionEnum;

import lombok.Data;

/** 排序字段实体类 */
@Data
public class OrderField {
    /** 排序字段名 */
    private String column;

    /** 排序方向 (asc/desc) */
    private SortDirectionEnum direction;

    /**
     * {@code OrderField} 创建并初始化当前类型实例。
     */
    public OrderField() {}

    /**
     * {@code OrderField} 创建并初始化当前类型实例。
     *
     * @param column 参数值，用于执行当前操作。
     * @param direction 参数值，用于执行当前操作。
     */
    public OrderField(String column, SortDirectionEnum direction) {
        this.column = column;
        this.direction = direction;
    }

    /**
     * {@code OrderField} 创建并初始化当前类型实例。
     *
     * @param column 参数值，用于执行当前操作。
     * @param directionCode 参数值，用于执行当前操作。
     */
    public OrderField(String column, String directionCode) {
        this.column = column;
        this.direction = SortDirectionEnum.fromCode(directionCode);
    }
}
