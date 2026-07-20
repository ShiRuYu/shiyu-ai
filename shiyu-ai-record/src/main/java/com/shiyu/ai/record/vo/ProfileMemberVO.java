package com.shiyu.ai.record.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.record.bo.ProfileMemberBO;
/**
 * 人物成员关系视图对象
 */
@Data

@AutoMapper(target = ProfileMemberBO.class)
public class ProfileMemberVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 人物ID
     */
    private Long profileId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 角色（owner/parent/viewer）
     */
    private String role;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
