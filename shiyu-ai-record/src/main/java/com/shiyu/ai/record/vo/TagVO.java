package com.shiyu.ai.record.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.bo.record.TagBO;
/**
 * 标签视图对象
 */
@Data

@AutoMapper(target = TagBO.class)
public class TagVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
