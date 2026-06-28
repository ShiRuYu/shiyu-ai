package com.shiyu.ai.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 标签数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "tag")
public class TagDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 标签名称
     */
    private String name;
}
