package com.shiyu.ai.agent.domain.request;

import com.shiyu.ai.agent.domain.bo.UserBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AutoMapper(target = UserBO.class, reverseConvertGenerate = false)
public class UserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String nickName;

    private String email;

    private String phone;

    private String sex;

    private String avatar;

    private String status;

    private String remark;

    private Long[] roleIds;

    private Long[] postIds;
}
