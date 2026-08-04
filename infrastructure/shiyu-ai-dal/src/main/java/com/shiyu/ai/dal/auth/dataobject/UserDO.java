package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import com.shiyu.ai.auth.domain.model.UserBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 用户数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_user")
@AutoMapper(target = UserBO.class, reverseConvertGenerate = true)
public class UserDO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Id(keyType = KeyType.Auto)
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
     * 手机号
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息（JSON格式）
     */
    private String extInfo;
}
