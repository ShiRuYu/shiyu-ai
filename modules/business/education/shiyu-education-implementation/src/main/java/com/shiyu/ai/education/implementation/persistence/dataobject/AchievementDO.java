package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.AchievementBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * {@code AchievementDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("edu_achievement")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AchievementBO.class, reverseConvertGenerate = true)
public class AchievementDO extends TenantEntity {
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
     * 学生标识，表示当前对象中的对应属性。
     */
    private Long studentId;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * icon 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String icon;
    /**
     * earnedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime earnedAt;
}
