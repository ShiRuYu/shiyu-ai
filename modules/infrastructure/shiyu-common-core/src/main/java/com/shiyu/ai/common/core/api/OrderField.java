package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.SortDirectionEnum;
import lombok.Data;

/**
 * 排序字段实体类
 */
@Data
public class OrderField {
    /**
     * 排序字段名
     */
    private String column;

    /**
     * 排序方向 (asc/desc)
     */
    private SortDirectionEnum direction;

    public OrderField() {
    }

    public OrderField(String column, SortDirectionEnum direction) {
        this.column = column;
        this.direction = direction;
    }

    public OrderField(String column, String directionCode) {
        this.column = column;
        this.direction = SortDirectionEnum.fromCode(directionCode);
    }
}