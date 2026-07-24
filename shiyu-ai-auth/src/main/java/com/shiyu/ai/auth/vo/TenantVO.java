package com.shiyu.ai.auth.vo;

import com.shiyu.ai.dal.auth.bo.TenantBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AutoMapper(target = TenantBO.class)
public class TenantVO implements Serializable {

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 子租户列表 */
    private List<TenantVO> children;
}
