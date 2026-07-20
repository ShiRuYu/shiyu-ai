package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.QuestionDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Question 业务对象
 */
@AutoMapper(target = QuestionDO.class, reverseConvertGenerate = true)
@Data
public class QuestionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String type;

    private String subjectCode;

    private Integer grade;

    private Integer difficulty;

    private String abilityDimension;

    private String title;

    private String options;

    private String answer;

    private String analysis;

    private String source;

    private String tags;

    private Long usedCount;

}
