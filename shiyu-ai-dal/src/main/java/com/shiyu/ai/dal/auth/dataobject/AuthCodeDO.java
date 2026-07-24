package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 权限码数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_code")
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
