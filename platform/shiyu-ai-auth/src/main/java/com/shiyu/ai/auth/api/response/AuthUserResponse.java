package com.shiyu.ai.auth.api.response;
import lombok.Data;
@Data public class AuthUserResponse { private Long id; private String username; private String nickName; private String avatar; private String extInfo; private Integer status; private Integer delFlag; }
