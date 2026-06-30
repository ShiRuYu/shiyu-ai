package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Table("edu_question_knowledge")
public class QuestionKnowledgeDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long questionId;
    private Long knowledgeId;
    private Double weight;
}
