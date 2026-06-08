package com.shiyu.ai.common.core.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity基类
 */
@Data
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建者
     */
    private String createBy = "system";

    /**
     * 创建时间
     */
    private LocalDateTime createTime = LocalDateTime.now();

    /**
     * 更新者
     */
    private String updateBy = "system";

    /**
     * 更新时间
     */
    private LocalDateTime updateTime = LocalDateTime.now();

}
