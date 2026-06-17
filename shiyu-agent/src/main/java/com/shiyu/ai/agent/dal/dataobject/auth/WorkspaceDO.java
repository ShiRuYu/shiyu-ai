package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 工作空间数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "workspace")
public class WorkspaceDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 工作空间 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 父工作空间 ID
     */
    private Long parentId;

    /**
     * 工作空间名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer order;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0：正常 1：已删除）
     */
    private Integer delFlag;
}
