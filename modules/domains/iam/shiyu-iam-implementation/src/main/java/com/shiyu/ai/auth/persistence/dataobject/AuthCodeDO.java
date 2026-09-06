package com.shiyu.ai.auth.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import com.shiyu.ai.auth.domain.model.AuthCodeBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 权限码数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_auth_code")
@AutoMapper(target = AuthCodeBO.class, reverseConvertGenerate = true)
public class AuthCodeDO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限码 ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

}

