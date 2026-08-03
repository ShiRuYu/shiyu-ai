package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * StudyRecord 业务对象
 */
@Data
public class StudyRecordBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;



    private Long id;

    private Long studentId;

    private Long knowledgeId;

    private String recordType;

    private Long questionId;

    private Double score;

    private Double accuracy;

    private Integer durationSec;

    private LocalDateTime createTime;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
