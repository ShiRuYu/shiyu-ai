package com.shiyu.ai.common.foundation.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 实现 Page Data 相关的业务处理、协作逻辑或基础设施能力。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class PageData<T> implements Serializable {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 列表数据 */
    private List<T> items;

    /** 总记录数 */
    private long total;
}
