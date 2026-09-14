package com.shiyu.ai.iam.implementation.vo;

import com.shiyu.ai.iam.implementation.domain.model.TenantBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * {@code TenantVO} 承载平台模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@SuppressWarnings("serial")
@AutoMapper(target = TenantBO.class)
public class TenantVO implements Serializable {

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
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;

    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;

    /** 子租户列表 */
    private List<TenantVO> children;
}
