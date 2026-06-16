package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 记录标签关联数据对象
 */
@Data
@Table(value = "record_tag")
public class RecordTagDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 租户ID
     */
    private Long tenantId;
}
