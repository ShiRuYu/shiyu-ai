package com.shiyu.ai.common.foundation.api;

import com.shiyu.ai.common.foundation.enums.SortDirectionEnum;

import lombok.Data;

/**
 * 实现 Order Field 相关的业务处理、协作逻辑或基础设施能力。
 */
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
     * 执行 Order Field 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param column 用于完成本次业务处理的 column 参数。
     * @param direction 用于完成本次业务处理的 direction 参数。
     */
    public OrderField(String column, SortDirectionEnum direction) {
        this.column = column;
        this.direction = direction;
    }

    /**
     * 执行 Order Field 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param column 用于完成本次业务处理的 column 参数。
     * @param directionCode 用于完成本次业务处理的 directionCode 参数。
     */
    public OrderField(String column, String directionCode) {
        this.column = column;
        this.direction = SortDirectionEnum.fromCode(directionCode);
    }
}
