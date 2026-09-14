package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.TeacherBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code TeacherDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_teacher")
@AutoMapper(target = TeacherBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class TeacherDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;
    /**
     * teacherNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String teacherNo;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 学科，表示当前对象中的对应属性。
     */
    private String subject;
    /**
     * school 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String school;
    /**
     * 标题，表示当前对象中的对应属性。
     */
    private String title;
    /**
     * phone 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String phone;
    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;
}
