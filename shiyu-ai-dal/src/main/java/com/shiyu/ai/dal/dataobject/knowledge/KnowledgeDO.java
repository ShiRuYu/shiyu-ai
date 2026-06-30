package com.shiyu.ai.dal.dataobject.knowledge;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "knowledge")
public class KnowledgeDO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String code;

    private String name;

    private String description;

    private Integer difficulty;

    private String category;

    private String tags;

    private Integer status;
}
