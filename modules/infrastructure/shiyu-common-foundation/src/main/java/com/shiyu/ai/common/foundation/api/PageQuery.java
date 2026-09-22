package com.shiyu.ai.common.foundation.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 封装 Page 操作所需的请求条件和输入数据。
 */
@Data
@SuppressWarnings("serial")
public class PageQuery implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 分页大小 */
    private Integer pageSize;

    /** 当前页数 */
    private Integer pageNum;

    /** 排序列（单个） */
    private String orderByColumn;

    /** 排序的方向desc或者asc（单个） */
    private String isAsc;

    /** 多个排序字段列表 */
    private List<OrderField> orderFields;

    /** 过滤条件列表 */
    private List<FilterCondition> filterConditions;

    /** 当前记录起始索引 默认值 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 每页显示记录数 默认值 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 查询 Page 相关业务数据，并返回处理结果。
     *
     * @return 返回 Page 相关操作生成的结果数据。
     */
    @JsonIgnore
    public Integer getFirstNum() {
        int num = Objects.requireNonNullElse(pageNum, DEFAULT_PAGE_NUM);
        int size = Objects.requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);
        return (num - 1) * size;
    }

    /**
     * {@code PageQuery} 创建并初始化当前类型实例。
     */
    public PageQuery() {
        // 默认构造函数
    }

    /**
     * 查询 Page 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pageSize 每页返回的数据数量。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     */
    public PageQuery(Integer pageSize, Integer pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }
}
