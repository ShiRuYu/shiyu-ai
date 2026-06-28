package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.dal.dataobject.record.TagDO;
import com.shiyu.ai.common.core.validate.AddGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 标签业务对象
 */
@Data
@AutoMapper(target = TagDO.class, reverseConvertGenerate = true)
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
}
