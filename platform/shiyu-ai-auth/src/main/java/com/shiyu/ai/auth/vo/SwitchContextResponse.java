package com.shiyu.ai.auth.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 切换操作响应（角色/租户切换后返回完整上下文，消除 N+1 请求）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchContextResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 切换后的用户信息（含角色、extInfo 等）
     */
    private UserVO userInfo;

    /**
     * 用户所属租户列表（含子租户）
     */
    private List<TenantInfoVO> tenants;
}
