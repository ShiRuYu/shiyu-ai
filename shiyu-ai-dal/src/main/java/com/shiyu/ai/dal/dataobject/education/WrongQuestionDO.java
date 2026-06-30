package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("wrong_question")
public class WrongQuestionDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long questionId;
    private Long knowledgeId;
    private String studentAnswer;
    private Integer correctTimes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
