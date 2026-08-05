package com.shiyu.ai.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MediaRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @NotNull(message = "记录ID不能为空")
    private Long recordId;
    @NotBlank(message = "URL不能为空")
    private String url;
    private String mediaType;
}
