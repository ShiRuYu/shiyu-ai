package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReviewTask 业务对象
 */
@AutoMapper(target = ReviewTaskDO.class, reverseConvertGenerate = true)
@Data
public class ReviewTaskBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private Long knowledgeId;

    private Long questionId;

    private String status;

    private LocalDate reviewDate;

    private Integer reviewRound;

    private Double resultScore;

    private LocalDateTime completedAt;

}
