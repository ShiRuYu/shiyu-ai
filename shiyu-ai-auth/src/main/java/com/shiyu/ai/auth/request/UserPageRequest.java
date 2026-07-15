package com.shiyu.ai.auth.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户分页查询请求
 */
@Data
public class UserPageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private Integer pageNo;

    private Integer pageSize;
}
