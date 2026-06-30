package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("edu_question")
public class QuestionDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
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
    private Integer status;
    private Long usedCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
