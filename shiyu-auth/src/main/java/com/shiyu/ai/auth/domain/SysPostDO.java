package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.core.domain.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位表 sys_post
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_post")
public class SysPostDO extends TenantEntity {

    /**
     * 岗位序号
     */
    @Id
    private Long postId;

    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 岗位排序
     */
    private Integer postSort;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

}
