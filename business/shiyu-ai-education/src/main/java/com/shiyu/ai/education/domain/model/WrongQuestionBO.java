package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * WrongQuestion 业务对象
 */
@Data
public class WrongQuestionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private Long questionId;

    private Long knowledgeId;

    private String studentAnswer;

    private Integer correctTimes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
