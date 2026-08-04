package com.shiyu.ai.agent.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ModelPageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long platformId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
