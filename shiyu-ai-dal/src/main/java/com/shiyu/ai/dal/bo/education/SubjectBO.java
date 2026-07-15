package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.SubjectDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Subject 业务对象
 */
@AutoMapper(target = SubjectDO.class, reverseConvertGenerate = true)
@Data
public class SubjectBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String name;

    private String gradeLevel;

    private String icon;

    private Integer sortOrder;

}
