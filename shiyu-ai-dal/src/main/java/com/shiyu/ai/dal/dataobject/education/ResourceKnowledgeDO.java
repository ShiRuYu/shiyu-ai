package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Table("resource_knowledge")
public class ResourceKnowledgeDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long resourceId;
    private Long knowledgeId;
    private Integer sortOrder;
}
