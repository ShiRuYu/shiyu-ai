package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.dal.dataobject.record.RecordDO;
import com.shiyu.ai.common.core.validate.AddGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 记录内容业务对象
 */
@Data
@AutoMapper(target = RecordDO.class, reverseConvertGenerate = true)
public class RecordBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 事件ID
     */
    @NotNull(message = "事件ID不能为空", groups = { AddGroup.class })
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
