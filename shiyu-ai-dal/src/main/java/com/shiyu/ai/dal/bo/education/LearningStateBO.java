package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.LearningStateDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * LearningState 业务对象
 */
@AutoMapper(target = LearningStateDO.class, reverseConvertGenerate = true)
@Data
public class LearningStateBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long studentId;

    private Long knowledgeId;

    private String state;

}
