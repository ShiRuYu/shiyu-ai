package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.ChapterBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_chapter")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ChapterBO.class, reverseConvertGenerate = true)
public class ChapterDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long textbookId;
    private Long parentId;
    private String name;
    private Integer chapterOrder;
}

