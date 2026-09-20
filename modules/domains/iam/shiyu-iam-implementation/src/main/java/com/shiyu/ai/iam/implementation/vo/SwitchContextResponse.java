package com.shiyu.ai.iam.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 封装 Switch Context 操作向调用方返回的传输数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class SwitchContextResponse implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 切换后的用户信息（含角色、extInfo 等） */
    private UserVO userInfo;

    /** 用户所属租户列表（含子租户） */
    private List<TenantInfoVO> tenants;
}
