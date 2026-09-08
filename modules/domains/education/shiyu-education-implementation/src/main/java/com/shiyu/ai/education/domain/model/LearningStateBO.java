package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * LearningState 业务对象
 */
@Data
public class LearningStateBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private Long knowledgeId;

    private String state;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
