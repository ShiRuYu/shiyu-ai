package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.CourseSectionBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * {@code CourseSectionDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("edu_course_section")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CourseSectionBO.class, reverseConvertGenerate = true)
public class CourseSectionDO extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 章节标识，表示当前对象中的对应属性。
     */
    private Long chapterId;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 序号，表示当前对象中的对应属性。
     */
    private Integer orderNo;
    /**
     * 内容地址，表示当前对象中的对应属性。
     */
    private String contentUrl;
    /**
     * videoUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String videoUrl;
    /**
     * durationMin 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer durationMin;
    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;
}
