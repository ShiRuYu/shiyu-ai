package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_question")
public class QuestionDO extends TenantEntity {

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
    private Long usedCount;
}
