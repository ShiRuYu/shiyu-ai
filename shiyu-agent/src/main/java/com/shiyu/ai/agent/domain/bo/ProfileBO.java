package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.agent.dal.dataobject.record.ProfileDO;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 人物业务对象
 */
@Data
@AutoMapper(target = ProfileDO.class, reverseConvertGenerate = true)
public class ProfileBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 人物ID
     */
    @NotNull(message = "人物ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 性别（0男 1女 2未知）
     */
    private Integer gender;

    /**
     * 性别标签（接口返回用，不映射数据库）
     */
    private String genderLabel;

    /**
     * 出生日期
     */
    private Date birthDate;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 删除标志（0存在 1删除）
     */
    private Integer delFlag;
}
