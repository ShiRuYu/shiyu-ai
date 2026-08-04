package com.shiyu.ai.dal.record.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("record_record_tag")
public class RecordTagDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long tagId;
}
