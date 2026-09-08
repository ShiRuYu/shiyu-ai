package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class StudyRecordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "知识点ID不能为空")
    private Long knowledgeId;
    @NotBlank(message = "学习记录类型不能为空")
    private String recordType;
    private Long questionId;
    private Double score;
    private Double accuracy;
    private Integer durationSec;
}
