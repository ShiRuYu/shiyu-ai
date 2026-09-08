package com.shiyu.ai.agent.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PlatformPageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String code;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
