package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.CourseBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code CourseDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_course")
@AutoMapper(target = CourseBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class CourseDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;
    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;
    /**
     * textbookId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long textbookId;
    /**
     * 教师标识，表示当前对象中的对应属性。
     */
    private Long teacherId;
    /**
     * coverUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String coverUrl;
    /**
     * totalHours 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer totalHours;
    /**
     * viewCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long viewCount;
    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间，表示当前对象中的对应属性。
     */
    private LocalDateTime updatedAt;
}
