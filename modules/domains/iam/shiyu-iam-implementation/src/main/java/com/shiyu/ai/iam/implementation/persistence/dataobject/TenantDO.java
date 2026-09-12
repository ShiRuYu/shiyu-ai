package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code TenantDO} 是平台模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_tenant")
@AutoMapper(target = TenantBO.class, reverseConvertGenerate = true)
public class TenantDO extends BaseEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 父租户ID（null=根租户） */
    private Long parentId;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * contactName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String contactName;

    /**
     * contactPhone 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String contactPhone;

    /**
     * address 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String address;

    /**
     * domain 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String domain;

    /**
     * intro 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String intro;

    /** 排序 */
    private Integer order;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 备注 */
    private String remark;
}
