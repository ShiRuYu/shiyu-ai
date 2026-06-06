package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.agent.dal.dataobject.auth.DeptDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门业务对象
 */
@Data
@AutoMapper(target = DeptDO.class, reverseConvertGenerate = true)
public class DeptBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门 ID
     */
    private Long id;

    /**
     * 父部门 ID
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer order;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0：正常 1：已删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 子部门列表
     */
    private List<DeptBO> children;
}
