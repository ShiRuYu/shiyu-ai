package com.shiyu.ai.dal.dataobject.knowledge;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("knowledge_chunk")
public class KnowledgeChunkDO implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long documentId;

    private String content;

    private String embedding;

    private String metadata;

    private Integer chunkIndex;

    private String createBy;

    private LocalDateTime createTime;
}
