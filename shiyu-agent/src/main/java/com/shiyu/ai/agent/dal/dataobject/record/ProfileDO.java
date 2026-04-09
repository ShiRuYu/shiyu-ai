package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 人物数据对象
 */
@Data
@Table(value = "profile")
public class ProfileDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 人物ID
     */
    @Id(keyType = KeyType.Auto)
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
    private Date birthDate;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 删除标志（0存在 1删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
