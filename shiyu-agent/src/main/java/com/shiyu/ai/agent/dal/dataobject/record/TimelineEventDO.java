package com.shiyu.ai.agent.dal.dataobject.record;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 时间轴事件数据对象
 */
@Data
@Table(value = "timeline_event")
public class TimelineEventDO implements Serializable {

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
    private Date eventTime;

    /**
     * 事件类型（milestone/daily/custom）
     */
    private String type;

    /**
     * 可见性（private/family/public）
     */
    private String visibility;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private Date createTime;
}
