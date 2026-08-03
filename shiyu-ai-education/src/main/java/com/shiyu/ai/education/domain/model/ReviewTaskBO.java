package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReviewTask 业务对象
 */
@Data
public class ReviewTaskBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private Long knowledgeId;

    private Long questionId;

    private Integer status;

    private String statusDesc;

    private LocalDate reviewDate;

    private Integer reviewRound;

    private Double resultScore;

    private LocalDateTime completedAt;

}
