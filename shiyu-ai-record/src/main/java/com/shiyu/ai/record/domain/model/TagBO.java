package com.shiyu.ai.record.domain.model;

import com.shiyu.ai.common.core.validate.AddGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 标签业务对象
 */
@Data
public class TagBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空", groups = { AddGroup.class })
    private String name;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
