package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class ReviewRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "知识点ID不能为空")
    private Long knowledgeId;

    private Integer status;
    private LocalDate reviewDate;
    private Integer reviewRound;
    private Double resultScore;
    private LocalDateTime completedAt;
}
