package com.shiyu.ai.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.model.bo.LongTermMemoryBO;

@AutoMapper(target = LongTermMemoryBO.class, reverseConvertGenerate = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Table("long_term_memory")
public class LongTermMemoryDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private String agentId;

    private String category;

    private String memoryKey;

    private String content;

    private Double importance;

    private String source;

}
