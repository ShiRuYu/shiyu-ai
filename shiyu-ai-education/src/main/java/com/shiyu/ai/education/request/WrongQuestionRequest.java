package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WrongQuestionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    private Long knowledgeId;
    private String studentAnswer;
    private Integer correctTimes;
    private Integer status;
}
