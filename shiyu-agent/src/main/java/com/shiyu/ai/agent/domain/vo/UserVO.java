package com.shiyu.ai.agent.domain.vo;

import com.shiyu.ai.agent.domain.bo.UserBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户信息视图对象
 */
@Data
@AutoMapper(target = UserBO.class)
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 删除标志（0：正常 1：已删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 地址
     */
    private String address;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色列表
     */
    private List<RoleVO> roles;

    /**
     * 当前角色
     */
    private RoleVO currentRole;

    /**
     * 扩展信息（JSON格式）
     */
    private String extInfo;

    /**
     * 可用租户列表
     */
    private List<Map<String, Object>> tenants;

    /**
     * 可用工作空间列表
     */
    private List<WorkspaceContextVO> workspaces;

    /**
     * 当前租户ID
     */
    private Long currentTenantId;

    /**
     * 当前工作空间ID
     */
    private Long currentWorkspaceId;
}
