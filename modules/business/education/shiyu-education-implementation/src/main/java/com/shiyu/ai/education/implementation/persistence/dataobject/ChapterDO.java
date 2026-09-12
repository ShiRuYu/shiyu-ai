package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.ChapterBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code ChapterDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("edu_chapter")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ChapterBO.class, reverseConvertGenerate = true)
public class ChapterDO extends TenantEntity {

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
     * textbookId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long textbookId;
    /**
     * 父级标识，表示当前对象中的对应属性。
     */
    private Long parentId;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 章节顺序，表示当前对象中的对应属性。
     */
    private Integer chapterOrder;
}
