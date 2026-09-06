package com.shiyu.ai.common.core.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 分页查询实体类
 */
@Data
@SuppressWarnings("serial")
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 当前页数
     */
    private Integer pageNum;

    /**
     * 排序列（单个）
     */
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc（单个）
     */
    private String isAsc;

    /**
     * 多个排序字段列表
     */
    private List<OrderField> orderFields;

    /**
     * 过滤条件列表
     */
    private List<FilterCondition> filterConditions;

    /**
     * 当前记录起始索引 默认值
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 每页显示记录数 默认值
     */
    public static final int DEFAULT_PAGE_SIZE = 10;


    @JsonIgnore
    public Integer getFirstNum() {
        int num = Objects.requireNonNullElse(pageNum, DEFAULT_PAGE_NUM);
        int size = Objects.requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);
        return (num - 1) * size;
    }

    public PageQuery() {
        // 默认构造函数
    }

    public PageQuery(Integer pageSize, Integer pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }
}
