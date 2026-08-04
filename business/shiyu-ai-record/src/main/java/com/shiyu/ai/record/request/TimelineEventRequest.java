package com.shiyu.ai.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TimelineEventRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotNull(message = "档案ID不能为空")
    private Long profileId;
    @NotBlank(message = "事件标题不能为空")
    private String title;
    private Date eventDate;
    private String eventType;
}
