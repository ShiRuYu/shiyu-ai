package com.shiyu.ai.agent.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shiyu.ai.agent.domain.bo.DeptBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门请求对象
 */
@Data
@AutoMapper(target = DeptBO.class, reverseConvertGenerate = false)
public class DeptRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父部门 ID（前端字段名为 pid）
     */
    @JsonProperty("pid")
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
}
