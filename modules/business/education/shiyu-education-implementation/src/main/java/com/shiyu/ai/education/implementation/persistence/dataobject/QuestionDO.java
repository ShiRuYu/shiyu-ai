package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code QuestionDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_question")
@AutoMapper(target = QuestionBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class QuestionDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 类型，表示当前对象中的对应属性。
     */
    private String type;
    /**
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;
    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;
    /**
     * difficulty 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer difficulty;
    /**
     * abilityDimension 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String abilityDimension;
    /**
     * 标题，表示当前对象中的对应属性。
     */
    private String title;
    /**
     * 选项，表示当前对象中的对应属性。
     */
    private String options;
    /**
     * answer 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String answer;
    /**
     * analysis 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String analysis;
    /**
     * 来源，表示当前对象中的对应属性。
     */
    private String source;
    /**
     * tags 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String tags;
    /**
     * usedCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long usedCount;
}
