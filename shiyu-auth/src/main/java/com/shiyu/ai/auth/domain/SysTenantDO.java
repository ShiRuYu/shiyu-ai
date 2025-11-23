package com.shiyu.ai.auth.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 租户对象 sys_tenant
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_tenant")
public class SysTenantDO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 租户编号
     */
    private String tenantId;

    /**
     * 联系人
     */
    private String contactUserName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 统一社会信用代码
     */
    private String licenseNumber;

    /**
     * 地址
     */
    private String address;

    /**
     * 域名
     */
    private String domain;

    /**
     * 企业简介
     */
    private String intro;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户套餐编号
     */
    private Long packageId;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户数量（-1不限制）
     */
    private Long accountCount;

    /**
     * 租户状态（1正常 0停用）
     */
    private String status;

    /**
     * 删除标志（1代表存在 0代表删除）
     */
    private String delFlag;

}
