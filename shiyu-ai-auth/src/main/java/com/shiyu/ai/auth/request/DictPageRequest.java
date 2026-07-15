package com.shiyu.ai.auth.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典分页查询请求
 */
@Data
public class DictPageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer pageNo;

    private Integer pageSize;
}
