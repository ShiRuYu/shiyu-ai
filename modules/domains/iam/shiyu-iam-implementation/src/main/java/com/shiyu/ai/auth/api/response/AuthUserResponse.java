package com.shiyu.ai.auth.api.response;

import com.shiyu.ai.auth.domain.model.UserBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = UserBO.class)
public class AuthUserResponse {
    private Long id;
    private String username;
    private String nickName;
    private String avatar;
    private String extInfo;
    private Integer status;
    private Integer delFlag;
}
