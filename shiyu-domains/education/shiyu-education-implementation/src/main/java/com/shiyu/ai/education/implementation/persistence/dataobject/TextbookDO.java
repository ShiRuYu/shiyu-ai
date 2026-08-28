package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.TextbookBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_textbook")
@AutoMapper(target = TextbookBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class TextbookDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;
    private String subjectCode;
    private Integer grade;
    private String publisher;
    private String isbn;
    private LocalDateTime createTime;
}

