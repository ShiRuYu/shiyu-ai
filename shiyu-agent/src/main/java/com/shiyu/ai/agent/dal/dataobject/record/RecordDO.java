package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 记录内容数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "record")
public class RecordDO extends BaseEntity {

    /**
     * 记录ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * 记录内容
     */
    private String content;

    /**
     * 心情
     */
    private String mood;

    /**
     * 地点
     */
    private String location;

    /**
     * 天气
     */
    private String weather;
}
