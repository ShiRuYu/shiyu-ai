package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 人物成员关系数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "profile_member")
public class ProfileMemberDO extends BaseEntity {

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
}
