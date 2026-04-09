package com.shiyu.ai.agent.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果对象
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 总数
     */
    private Long total;

    public PageResult() {
    }

    public PageResult(List<T> items, Long total) {
        this.items = items;
        this.total = total;
    }
}
