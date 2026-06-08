package com.shiyu.ai.agent.dal.dataobject.common;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 字典数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "dict")
public class DictDO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 排序
     */
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
