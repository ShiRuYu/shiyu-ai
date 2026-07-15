package com.shiyu.ai.record.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RecordRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotNull(message = "事件ID不能为空")
    private Long eventId;
    private String content;
}
