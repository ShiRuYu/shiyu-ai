package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/**
 * 表示发起请求或拥有资源的用户唯一标识。
 * @param value 值，表示该记录组件承载的数据。
 */
public record UserId(long value) implements Serializable {

    public UserId {
        if (value <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}
