package com.shiyu.ai.record.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import com.shiyu.ai.record.domain.model.RecordTagBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("record_record_tag")
@AutoMapper(target = RecordTagBO.class, reverseConvertGenerate = true)
public class RecordTagDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long tagId;
}
