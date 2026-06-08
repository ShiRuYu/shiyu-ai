package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.agent.dal.dataobject.common.DictDO;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import com.shiyu.ai.common.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 字典业务对象 dict
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DictDO.class, reverseConvertGenerate = true)
public class DictBO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @NotNull(message = "字典ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(min = 0, max = 100, message = "字典类型长度不能超过{max}个字符")
    private String dictType;

    /**
     * 字典标签
     */
    @NotBlank(message = "字典标签不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(min = 0, max = 100, message = "字典标签长度不能超过{max}个字符")
    private String dictLabel;

    /**
     * 字典键值
     */
    @NotBlank(message = "字典键值不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(min = 0, max = 100, message = "字典键值长度不能超过{max}个字符")
    private String dictValue;

    /**
     * 字典排序
     */
    @NotNull(message = "字典排序不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer dictSort;

    /**
     * 样式属性
     */
    private String cssClass;

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    private String isDefault;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0存在 1删除）
     */
    private String delFlag;

}
