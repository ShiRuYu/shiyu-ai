package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/**
 * 封装 角色 Id 相关的不可变数据及其字段约束。
 */
public record RoleId(long value) implements Serializable {

    public RoleId {
        if (value <= 0) {
            throw new IllegalArgumentException("roleId must be positive");
        }
    }
}
