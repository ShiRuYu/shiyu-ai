package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.StudentDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * Student 业务对象
 */
@AutoMapper(target = StudentDO.class, reverseConvertGenerate = true)
@Data
public class StudentBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String studentNo;

    private String name;

    private Integer gender;

    private LocalDate birthDate;

    private Integer grade;

    private String gradeLevel;

    private String school;

    private String className;

    private Long parentId;

    private String learningStyle;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
