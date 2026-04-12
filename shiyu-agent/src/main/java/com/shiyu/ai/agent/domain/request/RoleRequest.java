package com.shiyu.ai.agent.domain.request;

import com.shiyu.ai.agent.domain.bo.RoleBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色请求对象
 */
@Data
@AutoMapper(target = RoleBO.class, reverseConvertGenerate = false)
public class RoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
