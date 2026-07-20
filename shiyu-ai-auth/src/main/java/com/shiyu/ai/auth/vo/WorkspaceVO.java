package com.shiyu.ai.auth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shiyu.ai.dal.auth.bo.WorkspaceBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作空间视图对象
 */
@Data
@AutoMapper(target = WorkspaceBO.class)
public class WorkspaceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonProperty("pid")
    private Long parentId;

    private String name;

    private Integer order;

    private String leader;

    private String phone;

    private String email;

    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<WorkspaceVO> children;
}
