package com.shiyu.ai.auth.api.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthCodeResponse {
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
