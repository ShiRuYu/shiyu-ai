package com.shiyu.ai.common.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页数据对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class PageData<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 列表数据
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private long total;
}
