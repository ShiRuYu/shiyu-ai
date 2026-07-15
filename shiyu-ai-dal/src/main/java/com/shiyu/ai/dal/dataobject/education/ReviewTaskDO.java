package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("edu_review_task")
public class ReviewTaskDO implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long knowledgeId;

    @Column(ignore = true)
    private Long questionId;

    private String status;
    private LocalDate reviewDate;
    private Integer reviewRound;
    private Double resultScore;
    private LocalDateTime completedAt;
}
