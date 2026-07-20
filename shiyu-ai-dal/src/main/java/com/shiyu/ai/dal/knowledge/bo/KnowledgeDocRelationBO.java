package com.shiyu.ai.dal.knowledge.bo;

import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocRelationDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@AutoMapper(target = KnowledgeDocRelationDO.class, reverseConvertGenerate = true)
@Data
public class KnowledgeDocRelationBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long docId;
    private Long knowledgeId;
    private String relationType;
    private LocalDateTime createTime;
}
