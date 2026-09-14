package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/**
 * 表示访问控制中的角色唯一标识。
 * @param value 值，表示该记录组件承载的数据。
 */
public record RoleId(long value) implements Serializable {

    public RoleId {
        if (value <= 0) {
            throw new IllegalArgumentException("roleId must be positive");
        }
    }
}
