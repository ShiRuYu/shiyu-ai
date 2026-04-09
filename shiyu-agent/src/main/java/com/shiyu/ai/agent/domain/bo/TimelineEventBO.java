package com.shiyu.ai.agent.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shiyu.ai.agent.dal.dataobject.record.TimelineEventDO;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 时间轴事件业务对象
 */
@Data
@AutoMapper(target = TimelineEventDO.class, reverseConvertGenerate = true)
public class TimelineEventBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    @NotNull(message = "事件ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 人物ID
     */
    @NotNull(message = "人物ID不能为空", groups = { AddGroup.class })
    private Long profileId;

    /**
     * 事件标题
     */
    @NotBlank(message = "事件标题不能为空", groups = { AddGroup.class })
    private String title;

    /**
     * 事件时间
     */
    @NotNull(message = "事件时间不能为空", groups = { AddGroup.class })
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
}
