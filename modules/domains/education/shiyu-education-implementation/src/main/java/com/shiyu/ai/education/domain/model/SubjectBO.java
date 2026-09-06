package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Subject 业务对象
 */
@Data
public class SubjectBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String name;

    private String gradeLevel;

    private String icon;

    private Integer sortOrder;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
