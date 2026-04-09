package com.shiyu.ai.agent.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 时间轴事件视图对象
 */
@Data
public class TimelineEventVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 记录内容
     */
    private RecordVO record;

    /**
     * 附件列表
     */
    private List<MediaVO> mediaList;
}
