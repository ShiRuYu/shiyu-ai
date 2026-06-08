package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.io.Serial;

/**
 * 人物数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "profile")
public class ProfileDO extends BaseEntity {

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
    private LocalDate birthDate;

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
}
