package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.AbilityBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * {@code AbilityDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("edu_ability")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AbilityBO.class, reverseConvertGenerate = true)
public class AbilityDO extends TenantEntity {

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
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;
    /**
     * remember 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double remember;
    /**
     * understand 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double understand;
    /**
     * apply 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double apply;
    /**
     * analyze 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double analyze;
    /**
     * evaluate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double evaluate;
    /**
     * createScore 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double createScore;
    /**
     * overallMastery 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double overallMastery;
    /**
     * lastUpdate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime lastUpdate;
}
