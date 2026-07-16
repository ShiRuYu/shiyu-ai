package com.shiyu.ai.record.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 人物视图对象
 */
@Data
import com.shiyu.ai.dal.bo.record.ProfileBO;
import io.github.linpeilie.annotations.AutoMapper;

@AutoMapper(target = ProfileBO.class)
public class ProfileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 人物ID
     */
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
