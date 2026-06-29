package com.shiyu.ai.auth.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量分配用户角色请求
 */
@Data
public class AssignUserRolesRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID 列表
     */
    private List<Long> userIds;
}
