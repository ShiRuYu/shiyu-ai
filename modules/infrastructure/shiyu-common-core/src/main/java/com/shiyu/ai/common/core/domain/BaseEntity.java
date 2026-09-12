package com.shiyu.ai.common.core.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Entity基类 */
@Data
public class BaseEntity implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 状态（依据业务灵活定义，参考对应业务枚举） */
    private Integer status;

    /** 删除标志（0：正常 1：已删除） */
    private Integer delFlag;
}
