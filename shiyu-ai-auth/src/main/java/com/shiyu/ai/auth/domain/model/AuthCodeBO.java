package com.shiyu.ai.auth.domain.model;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 权限码数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthCodeBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限码 ID
     */
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
