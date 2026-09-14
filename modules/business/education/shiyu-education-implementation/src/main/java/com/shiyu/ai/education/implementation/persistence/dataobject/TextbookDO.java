package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.TextbookBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code TextbookDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_textbook")
@AutoMapper(target = TextbookBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class TextbookDO extends TenantEntity {

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
     * 学科编码，表示当前对象中的对应属性。
     */
    private String subjectCode;
    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;
    /**
     * 发布器，表示当前对象中的对应属性。
     */
    private String publisher;
    /**
     * isbn 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String isbn;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}
