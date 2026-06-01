package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人物成员关系数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "profile_member")
public class ProfileMemberDO extends BaseEntity {

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
}
