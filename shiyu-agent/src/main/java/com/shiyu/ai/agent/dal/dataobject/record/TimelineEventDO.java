package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.io.Serial;

/**
 * 时间轴事件数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "timeline_event")
public class TimelineEventDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 人物ID
     */
    private Long profileId;

    /**
     * 事件标题
     */
    private String title;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
}
