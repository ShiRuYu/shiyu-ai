package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("student")
public class StudentDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
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
