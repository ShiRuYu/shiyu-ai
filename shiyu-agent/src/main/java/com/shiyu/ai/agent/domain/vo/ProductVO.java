package com.shiyu.ai.agent.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 产品表格视图对象 - 用于 /table/list 接口
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private String id;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 图片URL2
     */
    private String imageUrl2;

    /**
     * 是否打开
     */
    private Boolean open;

    /**
     * 状态（success/warning/error）
     */
    private String status;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 价格
     */
    private String price;

    /**
     * 货币
     */
    private String currency;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否可用
     */
    private Boolean available;

    /**
     * 分类
     */
    private String category;

    /**
     * 发布日期
     */
    private Date releaseDate;

    /**
     * 评分
     */
    private Double rating;

    /**
     * 描述
     */
    private String description;

    /**
     * 重量
     */
    private Double weight;

    /**
     * 颜色
     */
    private String color;

    /**
     * 是否在生产中
     */
    private Boolean inProduction;

    /**
     * 标签
     */
    private List<String> tags;
}
