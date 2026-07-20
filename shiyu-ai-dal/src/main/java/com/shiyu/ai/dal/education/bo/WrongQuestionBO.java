package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.WrongQuestionDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * WrongQuestion 业务对象
 */
@AutoMapper(target = WrongQuestionDO.class, reverseConvertGenerate = true)
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

}
