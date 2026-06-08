package com.shiyu.ai.agent.dal.dataobject.auth;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 部门数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "dept")
public class DeptDO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 父部门 ID
     */
    private Long parentId;

    /**
     * 部门名称
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
