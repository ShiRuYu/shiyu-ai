package com.shiyu.ai.iam.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * {@code TenantBO} 是模型模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@SuppressWarnings("serial")
public class TenantBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
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

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;

    /**
     * delFlag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer delFlag;

    /**
     * createBy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String createBy;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;

    /**
     * updateBy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String updateBy;

    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;

    /** 子租户列表 */
    private List<TenantBO> children;

    /**
     * menuIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<Long> menuIds;

    /**
     * authCodeIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<Long> authCodeIds;

    /**
     * adminRoleName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String adminRoleName;

    /**
     * adminUsername 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String adminUsername;

    /**
     * adminPassword 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String adminPassword;
}
