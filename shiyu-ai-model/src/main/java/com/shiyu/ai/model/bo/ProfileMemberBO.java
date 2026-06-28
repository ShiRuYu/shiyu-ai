package com.shiyu.ai.model.bo;

import com.shiyu.ai.common.core.validate.AddGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 人物成员关系业务对象
 */
@Data
public class ProfileMemberBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 人物ID
     */
    @NotNull(message = "人物ID不能为空", groups = { AddGroup.class })
    private Long profileId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class })
    private Long userId;

    /**
     * 角色（owner/parent/viewer）
     */
    @NotBlank(message = "角色不能为空", groups = { AddGroup.class })
    private String role;
}
