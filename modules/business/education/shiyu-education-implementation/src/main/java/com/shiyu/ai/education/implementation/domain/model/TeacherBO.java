package com.shiyu.ai.education.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code TeacherBO} 是教育模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class TeacherBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
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
