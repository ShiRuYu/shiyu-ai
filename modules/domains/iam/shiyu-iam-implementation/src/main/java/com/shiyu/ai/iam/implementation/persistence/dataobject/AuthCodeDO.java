package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.iam.implementation.domain.model.AuthCodeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/** 权限码数据对象 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_auth_code")
@AutoMapper(target = AuthCodeBO.class, reverseConvertGenerate = true)
public class AuthCodeDO extends BaseEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 权限码 ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 权限编码 */
    private String code;

    /** 权限名称 */
    private String name;
}
