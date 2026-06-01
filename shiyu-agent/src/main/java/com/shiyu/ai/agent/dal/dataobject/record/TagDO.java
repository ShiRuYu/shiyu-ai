package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "tag")
public class TagDO extends BaseEntity {

    /**
     * 标签ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 创建者ID
     */
    private Long creatorId;
}
