package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 标签数据对象
 */
@Data
@Table(value = "tag")
public class TagDO implements Serializable {

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

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Date createTime;
}
