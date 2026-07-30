package com.shiyu.ai.dal.auth.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;

@AutoMapper(target = TenantDO.class, reverseConvertGenerate = true)
@Data
public class TenantBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /** 父租户ID（null=根租户） */
    private Long parentId;

    private String code;

    private String name;

    private String contactName;

    private String contactPhone;

    private String address;

    private String domain;

    private String intro;

    /** 排序 */
    private Integer order;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 备注 */
    private String remark;

    private Integer status;

    private Integer delFlag;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    /** 子租户列表 */
    private List<TenantBO> children;

    private List<Long> menuIds;

    private List<Long> authCodeIds;

    private String adminRoleName;

    private String adminUsername;

    private String adminPassword;
}
