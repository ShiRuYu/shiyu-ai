package com.shiyu.ai.iam.implementation.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 封装 用户 Info 操作向调用方返回的传输数据。
 */
@Data
@SuppressWarnings("serial")
public class UserInfoVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;

    /** 密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色列表 */
    private List<String> roles;

    /** 用户名 */
    private String username;

    /** 首页路径（可选） */
    private String homePath;
}
