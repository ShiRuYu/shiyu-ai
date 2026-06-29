package com.shiyu.ai.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户分页列表响应对象
 */
@Data
public class UserPageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户列表
     */
    private List<UserVO> items;

    /**
     * 总数
     */
    private Long total;
}
