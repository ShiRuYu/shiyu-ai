package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户和岗位关联 sys_user_post
 *
 */

@Data
@Table("sys_user_post")
public class SysUserPostDO {

    /**
     * 用户ID
     */
    @Id
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;

}
