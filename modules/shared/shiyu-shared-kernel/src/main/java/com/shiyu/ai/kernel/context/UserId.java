package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/**
 * 封装 用户 Id 相关的不可变数据及其字段约束。
 */
public record UserId(long value) implements Serializable {

    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}
