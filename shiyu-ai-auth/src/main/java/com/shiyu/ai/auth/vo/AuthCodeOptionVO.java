package com.shiyu.ai.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限码下拉及授权选项。
 */
@Data
public class AuthCodeOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String code;

    private String module;

    private String resource;

    private String action;

    private Integer status;

    private LocalDateTime createTime;
}
