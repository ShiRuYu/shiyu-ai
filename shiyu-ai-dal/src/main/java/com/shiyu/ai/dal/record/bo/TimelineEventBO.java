package com.shiyu.ai.dal.record.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.record.dataobject.TimelineEventDO;

/**
 * 时间轴事件业务对象
 */
@AutoMapper(target = TimelineEventDO.class, reverseConvertGenerate = true)
@Data
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
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}
