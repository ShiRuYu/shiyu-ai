package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 人物成员关系数据对象
 */
@Data
@Table(value = "profile_member")
public class ProfileMemberDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 人物ID
     */
    private Long profileId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色（owner/parent/viewer）
     */
    private String role;

    /**
     * 创建时间
     */
    private Date createTime;
}
