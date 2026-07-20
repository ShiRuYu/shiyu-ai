package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.StudyRecordDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * StudyRecord 业务对象
 */
@AutoMapper(target = StudyRecordDO.class, reverseConvertGenerate = true)
@Data
public class StudyRecordBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;



    private Long id;

    private Long studentId;

    private Long knowledgeId;

    private String recordType;

    private Long questionId;

    private Double score;

    private Double accuracy;

    private Integer durationSec;

    private LocalDateTime createTime;

}
